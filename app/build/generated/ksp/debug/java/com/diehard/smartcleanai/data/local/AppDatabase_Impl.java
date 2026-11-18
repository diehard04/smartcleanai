package com.diehard.smartcleanai.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ScannedMediaDao _scannedMediaDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `scanned_media` (`mediaId` INTEGER NOT NULL, `uri` TEXT NOT NULL, `displayName` TEXT, `bucketName` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `dateModified` INTEGER NOT NULL, `mimeType` TEXT, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `pHash` INTEGER, `blurScore` REAL, `category` TEXT NOT NULL, `groupId` TEXT, `isBestShot` INTEGER NOT NULL, `isIgnored` INTEGER NOT NULL, `sha256` TEXT, PRIMARY KEY(`mediaId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scanned_media_dateModified` ON `scanned_media` (`dateModified`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scanned_media_category` ON `scanned_media` (`category`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scanned_media_groupId` ON `scanned_media` (`groupId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scanned_media_isIgnored` ON `scanned_media` (`isIgnored`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '828660550373a2398999bc0a0b66e1f0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `scanned_media`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsScannedMedia = new HashMap<String, TableInfo.Column>(16);
        _columnsScannedMedia.put("mediaId", new TableInfo.Column("mediaId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("uri", new TableInfo.Column("uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("displayName", new TableInfo.Column("displayName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("bucketName", new TableInfo.Column("bucketName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("dateModified", new TableInfo.Column("dateModified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("mimeType", new TableInfo.Column("mimeType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("width", new TableInfo.Column("width", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("height", new TableInfo.Column("height", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("pHash", new TableInfo.Column("pHash", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("blurScore", new TableInfo.Column("blurScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("groupId", new TableInfo.Column("groupId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("isBestShot", new TableInfo.Column("isBestShot", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("isIgnored", new TableInfo.Column("isIgnored", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedMedia.put("sha256", new TableInfo.Column("sha256", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScannedMedia = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScannedMedia = new HashSet<TableInfo.Index>(4);
        _indicesScannedMedia.add(new TableInfo.Index("index_scanned_media_dateModified", false, Arrays.asList("dateModified"), Arrays.asList("ASC")));
        _indicesScannedMedia.add(new TableInfo.Index("index_scanned_media_category", false, Arrays.asList("category"), Arrays.asList("ASC")));
        _indicesScannedMedia.add(new TableInfo.Index("index_scanned_media_groupId", false, Arrays.asList("groupId"), Arrays.asList("ASC")));
        _indicesScannedMedia.add(new TableInfo.Index("index_scanned_media_isIgnored", false, Arrays.asList("isIgnored"), Arrays.asList("ASC")));
        final TableInfo _infoScannedMedia = new TableInfo("scanned_media", _columnsScannedMedia, _foreignKeysScannedMedia, _indicesScannedMedia);
        final TableInfo _existingScannedMedia = TableInfo.read(db, "scanned_media");
        if (!_infoScannedMedia.equals(_existingScannedMedia)) {
          return new RoomOpenHelper.ValidationResult(false, "scanned_media(com.diehard.smartcleanai.data.local.ScannedMediaEntity).\n"
                  + " Expected:\n" + _infoScannedMedia + "\n"
                  + " Found:\n" + _existingScannedMedia);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "828660550373a2398999bc0a0b66e1f0", "c2158ad9c8548251a550fe8f1daef45b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "scanned_media");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `scanned_media`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ScannedMediaDao.class, ScannedMediaDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ScannedMediaDao scannedMediaDao() {
    if (_scannedMediaDao != null) {
      return _scannedMediaDao;
    } else {
      synchronized(this) {
        if(_scannedMediaDao == null) {
          _scannedMediaDao = new ScannedMediaDao_Impl(this);
        }
        return _scannedMediaDao;
      }
    }
  }
}
