package com.example.smartcleanai.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScannedMediaDao_Impl implements ScannedMediaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScannedMediaEntity> __insertionAdapterOfScannedMediaEntity;

  private final EntityDeletionOrUpdateAdapter<ScannedMediaEntity> __updateAdapterOfScannedMediaEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateIgnored;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ScannedMediaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScannedMediaEntity = new EntityInsertionAdapter<ScannedMediaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scanned_media` (`mediaId`,`uri`,`displayName`,`bucketName`,`fileSize`,`dateModified`,`mimeType`,`width`,`height`,`pHash`,`blurScore`,`category`,`groupId`,`isBestShot`,`isIgnored`,`sha256`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScannedMediaEntity entity) {
        statement.bindLong(1, entity.getMediaId());
        statement.bindString(2, entity.getUri());
        if (entity.getDisplayName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDisplayName());
        }
        statement.bindString(4, entity.getBucketName());
        statement.bindLong(5, entity.getFileSize());
        statement.bindLong(6, entity.getDateModified());
        if (entity.getMimeType() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getMimeType());
        }
        statement.bindLong(8, entity.getWidth());
        statement.bindLong(9, entity.getHeight());
        if (entity.getPHash() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getPHash());
        }
        if (entity.getBlurScore() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getBlurScore());
        }
        statement.bindString(12, entity.getCategory());
        if (entity.getGroupId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getGroupId());
        }
        final int _tmp = entity.isBestShot() ? 1 : 0;
        statement.bindLong(14, _tmp);
        final int _tmp_1 = entity.isIgnored() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        if (entity.getSha256() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSha256());
        }
      }
    };
    this.__updateAdapterOfScannedMediaEntity = new EntityDeletionOrUpdateAdapter<ScannedMediaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `scanned_media` SET `mediaId` = ?,`uri` = ?,`displayName` = ?,`bucketName` = ?,`fileSize` = ?,`dateModified` = ?,`mimeType` = ?,`width` = ?,`height` = ?,`pHash` = ?,`blurScore` = ?,`category` = ?,`groupId` = ?,`isBestShot` = ?,`isIgnored` = ?,`sha256` = ? WHERE `mediaId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScannedMediaEntity entity) {
        statement.bindLong(1, entity.getMediaId());
        statement.bindString(2, entity.getUri());
        if (entity.getDisplayName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDisplayName());
        }
        statement.bindString(4, entity.getBucketName());
        statement.bindLong(5, entity.getFileSize());
        statement.bindLong(6, entity.getDateModified());
        if (entity.getMimeType() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getMimeType());
        }
        statement.bindLong(8, entity.getWidth());
        statement.bindLong(9, entity.getHeight());
        if (entity.getPHash() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getPHash());
        }
        if (entity.getBlurScore() == null) {
          statement.bindNull(11);
        } else {
          statement.bindDouble(11, entity.getBlurScore());
        }
        statement.bindString(12, entity.getCategory());
        if (entity.getGroupId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getGroupId());
        }
        final int _tmp = entity.isBestShot() ? 1 : 0;
        statement.bindLong(14, _tmp);
        final int _tmp_1 = entity.isIgnored() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        if (entity.getSha256() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSha256());
        }
        statement.bindLong(17, entity.getMediaId());
      }
    };
    this.__preparedStmtOfUpdateIgnored = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE scanned_media SET isIgnored = ? WHERE mediaId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scanned_media";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<ScannedMediaEntity> entities,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScannedMediaEntity.insert(entities);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final ScannedMediaEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScannedMediaEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ScannedMediaEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfScannedMediaEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateIgnored(final long mediaId, final boolean isIgnored,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateIgnored.acquire();
        int _argIndex = 1;
        final int _tmp = isIgnored ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, mediaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateIgnored.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScannedMediaEntity>> getAllActiveMedia() {
    final String _sql = "SELECT * FROM scanned_media WHERE isIgnored = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scanned_media"}, new Callable<List<ScannedMediaEntity>>() {
      @Override
      @NonNull
      public List<ScannedMediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final List<ScannedMediaEntity> _result = new ArrayList<ScannedMediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedMediaEntity _item;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _item = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllEntitiesDirect(
      final Continuation<? super List<ScannedMediaEntity>> $completion) {
    final String _sql = "SELECT * FROM scanned_media";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScannedMediaEntity>>() {
      @Override
      @NonNull
      public List<ScannedMediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final List<ScannedMediaEntity> _result = new ArrayList<ScannedMediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedMediaEntity _item;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _item = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllCacheChecks(final Continuation<? super List<MediaCacheCheck>> $completion) {
    final String _sql = "SELECT mediaId, dateModified FROM scanned_media";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaCacheCheck>>() {
      @Override
      @NonNull
      public List<MediaCacheCheck> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = 0;
          final int _cursorIndexOfDateModified = 1;
          final List<MediaCacheCheck> _result = new ArrayList<MediaCacheCheck>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaCacheCheck _item;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _item = new MediaCacheCheck(_tmpMediaId,_tmpDateModified);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScannedMediaEntity>> getMediaByCategory(final String category) {
    final String _sql = "SELECT * FROM scanned_media WHERE category = ? AND isIgnored = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scanned_media"}, new Callable<List<ScannedMediaEntity>>() {
      @Override
      @NonNull
      public List<ScannedMediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final List<ScannedMediaEntity> _result = new ArrayList<ScannedMediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedMediaEntity _item;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _item = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMediaByCategoryDirect(final String category,
      final Continuation<? super List<ScannedMediaEntity>> $completion) {
    final String _sql = "SELECT * FROM scanned_media WHERE category = ? AND isIgnored = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScannedMediaEntity>>() {
      @Override
      @NonNull
      public List<ScannedMediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final List<ScannedMediaEntity> _result = new ArrayList<ScannedMediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedMediaEntity _item;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _item = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScannedMediaEntity>> getIgnoredMedia() {
    final String _sql = "SELECT * FROM scanned_media WHERE isIgnored = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scanned_media"}, new Callable<List<ScannedMediaEntity>>() {
      @Override
      @NonNull
      public List<ScannedMediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final List<ScannedMediaEntity> _result = new ArrayList<ScannedMediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedMediaEntity _item;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _item = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getEntityById(final long mediaId,
      final Continuation<? super ScannedMediaEntity> $completion) {
    final String _sql = "SELECT * FROM scanned_media WHERE mediaId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mediaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScannedMediaEntity>() {
      @Override
      @Nullable
      public ScannedMediaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final ScannedMediaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _result = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getEntitiesByIds(final List<Long> ids,
      final Continuation<? super List<ScannedMediaEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM scanned_media WHERE mediaId IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : ids) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScannedMediaEntity>>() {
      @Override
      @NonNull
      public List<ScannedMediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfUri = CursorUtil.getColumnIndexOrThrow(_cursor, "uri");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModified");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfPHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pHash");
          final int _cursorIndexOfBlurScore = CursorUtil.getColumnIndexOrThrow(_cursor, "blurScore");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfGroupId = CursorUtil.getColumnIndexOrThrow(_cursor, "groupId");
          final int _cursorIndexOfIsBestShot = CursorUtil.getColumnIndexOrThrow(_cursor, "isBestShot");
          final int _cursorIndexOfIsIgnored = CursorUtil.getColumnIndexOrThrow(_cursor, "isIgnored");
          final int _cursorIndexOfSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "sha256");
          final List<ScannedMediaEntity> _result = new ArrayList<ScannedMediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedMediaEntity _item_1;
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpUri;
            _tmpUri = _cursor.getString(_cursorIndexOfUri);
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final Long _tmpPHash;
            if (_cursor.isNull(_cursorIndexOfPHash)) {
              _tmpPHash = null;
            } else {
              _tmpPHash = _cursor.getLong(_cursorIndexOfPHash);
            }
            final Float _tmpBlurScore;
            if (_cursor.isNull(_cursorIndexOfBlurScore)) {
              _tmpBlurScore = null;
            } else {
              _tmpBlurScore = _cursor.getFloat(_cursorIndexOfBlurScore);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpGroupId;
            if (_cursor.isNull(_cursorIndexOfGroupId)) {
              _tmpGroupId = null;
            } else {
              _tmpGroupId = _cursor.getString(_cursorIndexOfGroupId);
            }
            final boolean _tmpIsBestShot;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBestShot);
            _tmpIsBestShot = _tmp != 0;
            final boolean _tmpIsIgnored;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIgnored);
            _tmpIsIgnored = _tmp_1 != 0;
            final String _tmpSha256;
            if (_cursor.isNull(_cursorIndexOfSha256)) {
              _tmpSha256 = null;
            } else {
              _tmpSha256 = _cursor.getString(_cursorIndexOfSha256);
            }
            _item_1 = new ScannedMediaEntity(_tmpMediaId,_tmpUri,_tmpDisplayName,_tmpBucketName,_tmpFileSize,_tmpDateModified,_tmpMimeType,_tmpWidth,_tmpHeight,_tmpPHash,_tmpBlurScore,_tmpCategory,_tmpGroupId,_tmpIsBestShot,_tmpIsIgnored,_tmpSha256);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByIds(final List<Long> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM scanned_media WHERE mediaId IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
