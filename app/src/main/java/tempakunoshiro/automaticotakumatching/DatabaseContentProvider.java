package tempakunoshiro.automaticotakumatching;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;
import tempakunoshiro.automaticotakumatching.DatabaseContent.ScreamColumns;

/**
 * Created by Nakano on 2016/09/16.
 */
public class DatabaseContentProvider extends ContentProvider {
    private static final int USER_LIST = 1;
    private static final int USER_ID = 2;
    private static final int SCREAM_LIST = 10;
    private static final int SCREAM_ID = 11;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(DatabaseContent.AUTHORITY, "user", USER_LIST);
        uriMatcher.addURI(DatabaseContent.AUTHORITY, "user/#", USER_ID);
        uriMatcher.addURI(DatabaseContent.AUTHORITY, "scream", SCREAM_LIST);
        uriMatcher.addURI(DatabaseContent.AUTHORITY, "scream/#", SCREAM_ID);
    }

    private static DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case USER_LIST:
                queryBuilder.setTables(DatabaseSchema.TBL_USER);
                break;
            case USER_ID:
                queryBuilder.setTables(DatabaseSchema.TBL_USER);
                queryBuilder.appendWhere(UserColumns._ID + " = " + uri.getLastPathSegment());
                break;
            case SCREAM_LIST:
                queryBuilder.setTables(DatabaseSchema.TBL_SCREAM);
                break;
            case SCREAM_ID:
                queryBuilder.setTables(DatabaseSchema.TBL_SCREAM);
                // limit query to one row at most:
                queryBuilder.appendWhere(ScreamColumns._ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case USER_LIST:
                return UserColumns.CONTENT_TYPE;
            case USER_ID:
                return UserColumns.CONTENT_USER_TYPE;
            case SCREAM_LIST:
                return ScreamColumns.CONTENT_TYPE;
            case SCREAM_ID:
                return ScreamColumns.CONTENT_SCREAM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        if (uriType != USER_LIST && uriType != SCREAM_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (uriType == USER_LIST) {
            long id = db.insert(DatabaseSchema.TBL_USER, null, values);
            return getUriForId(id, uri);
        } else {
            long id = db.insertWithOnConflict(DatabaseSchema.TBL_SCREAM, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return getUriForId(id, uri);
        }
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int delCount = 0;
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case USER_LIST:
                delCount = db.delete(DatabaseSchema.TBL_USER, selection, selectionArgs);
                break;
            case USER_ID:
                String userIdStr = uri.getLastPathSegment();
                String userWhere = UserColumns._ID + " = " + userIdStr;
                if (!TextUtils.isEmpty(selection)) {
                    userWhere += " AND " + selection;
                }
                delCount = db.delete(DatabaseSchema.TBL_USER, userWhere, selectionArgs);
                break;
            case SCREAM_LIST:
                delCount = db.delete(DatabaseSchema.TBL_SCREAM, selection, selectionArgs);
                break;
            case SCREAM_ID:
                String screamIdStr = uri.getLastPathSegment();
                String screamWhere = ScreamColumns._ID + " = " + screamIdStr;
                if (!TextUtils.isEmpty(selection)) {
                    screamWhere += " AND " + selection;
                }
                delCount = db.delete(DatabaseSchema.TBL_SCREAM, screamWhere, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int updateCount = 0;
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case USER_LIST:
                updateCount = db.update(DatabaseSchema.TBL_USER, values, selection, selectionArgs);
                break;
            case USER_ID:
                String userIdStr = uri.getLastPathSegment();
                String userWhere = UserColumns._ID + " = " + userIdStr;
                if (!TextUtils.isEmpty(selection)) {
                    userWhere += " AND " + selection;
                }
                updateCount = db.update(DatabaseSchema.TBL_USER, values, userWhere, selectionArgs);
                break;
            case SCREAM_LIST:
                updateCount = db.update(DatabaseSchema.TBL_SCREAM, values, selection, selectionArgs);
                break;
            case SCREAM_ID:
                String screamIdStr = uri.getLastPathSegment();
                String screamWhere = ScreamColumns._ID + " = " + screamIdStr;
                if (!TextUtils.isEmpty(selection)) {
                    screamWhere += " AND " + selection;
                }
                updateCount = db.update(DatabaseSchema.TBL_SCREAM, values, screamWhere, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
