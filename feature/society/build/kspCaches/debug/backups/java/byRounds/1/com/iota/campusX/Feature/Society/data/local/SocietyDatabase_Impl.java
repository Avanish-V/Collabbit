package com.iota.campusX.Feature.Society.data.local;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SocietyDatabase_Impl extends SocietyDatabase {
  private volatile CommunityDao _communityDao;

  private volatile UserCacheDao _userCacheDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(12) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `communities` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `logoUrl` TEXT, `memberCount` INTEGER NOT NULL, `category` TEXT NOT NULL, `creatorId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `isSynced` INTEGER NOT NULL, `isJoined` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `pinnedMessageId` TEXT, `pinnedMessageText` TEXT, `unreadCount` INTEGER NOT NULL, `lastMessageText` TEXT, `lastMessageTime` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `society_messages` (`id` TEXT NOT NULL, `societyId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `senderName` TEXT NOT NULL, `senderAvatarUrl` TEXT, `text` TEXT NOT NULL, `type` TEXT NOT NULL, `mediaUrl` TEXT, `thumbnailUrl` TEXT, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `aspectRatio` REAL NOT NULL, `fileName` TEXT, `timestamp` INTEGER NOT NULL, `replyToId` TEXT, `replyToText` TEXT, `replyToName` TEXT, `reactions` TEXT NOT NULL, `isRead` INTEGER NOT NULL, `isSending` INTEGER NOT NULL, `isFailed` INTEGER NOT NULL, `uploadProgress` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_cache` (`uid` TEXT NOT NULL, `name` TEXT NOT NULL, `avatarUrl` TEXT, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`uid`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '26254704b20aca1a6a298f32d895e3a9')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `communities`");
        db.execSQL("DROP TABLE IF EXISTS `society_messages`");
        db.execSQL("DROP TABLE IF EXISTS `user_cache`");
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
        final HashMap<String, TableInfo.Column> _columnsCommunities = new HashMap<String, TableInfo.Column>(16);
        _columnsCommunities.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("logoUrl", new TableInfo.Column("logoUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("memberCount", new TableInfo.Column("memberCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("creatorId", new TableInfo.Column("creatorId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("isSynced", new TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("isJoined", new TableInfo.Column("isJoined", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("pinnedMessageId", new TableInfo.Column("pinnedMessageId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("pinnedMessageText", new TableInfo.Column("pinnedMessageText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("lastMessageText", new TableInfo.Column("lastMessageText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommunities.put("lastMessageTime", new TableInfo.Column("lastMessageTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCommunities = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCommunities = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCommunities = new TableInfo("communities", _columnsCommunities, _foreignKeysCommunities, _indicesCommunities);
        final TableInfo _existingCommunities = TableInfo.read(db, "communities");
        if (!_infoCommunities.equals(_existingCommunities)) {
          return new RoomOpenHelper.ValidationResult(false, "communities(com.iota.campusX.Feature.Society.data.local.CommunityEntity).\n"
                  + " Expected:\n" + _infoCommunities + "\n"
                  + " Found:\n" + _existingCommunities);
        }
        final HashMap<String, TableInfo.Column> _columnsSocietyMessages = new HashMap<String, TableInfo.Column>(22);
        _columnsSocietyMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("societyId", new TableInfo.Column("societyId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("senderName", new TableInfo.Column("senderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("senderAvatarUrl", new TableInfo.Column("senderAvatarUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("mediaUrl", new TableInfo.Column("mediaUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("thumbnailUrl", new TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("width", new TableInfo.Column("width", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("height", new TableInfo.Column("height", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("aspectRatio", new TableInfo.Column("aspectRatio", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("fileName", new TableInfo.Column("fileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("replyToId", new TableInfo.Column("replyToId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("replyToText", new TableInfo.Column("replyToText", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("replyToName", new TableInfo.Column("replyToName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("reactions", new TableInfo.Column("reactions", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("isRead", new TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("isSending", new TableInfo.Column("isSending", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("isFailed", new TableInfo.Column("isFailed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocietyMessages.put("uploadProgress", new TableInfo.Column("uploadProgress", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSocietyMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSocietyMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSocietyMessages = new TableInfo("society_messages", _columnsSocietyMessages, _foreignKeysSocietyMessages, _indicesSocietyMessages);
        final TableInfo _existingSocietyMessages = TableInfo.read(db, "society_messages");
        if (!_infoSocietyMessages.equals(_existingSocietyMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "society_messages(com.iota.campusX.Feature.Society.data.local.SocietyMessageEntity).\n"
                  + " Expected:\n" + _infoSocietyMessages + "\n"
                  + " Found:\n" + _existingSocietyMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsUserCache = new HashMap<String, TableInfo.Column>(4);
        _columnsUserCache.put("uid", new TableInfo.Column("uid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserCache.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserCache.put("avatarUrl", new TableInfo.Column("avatarUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserCache.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserCache = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserCache = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserCache = new TableInfo("user_cache", _columnsUserCache, _foreignKeysUserCache, _indicesUserCache);
        final TableInfo _existingUserCache = TableInfo.read(db, "user_cache");
        if (!_infoUserCache.equals(_existingUserCache)) {
          return new RoomOpenHelper.ValidationResult(false, "user_cache(com.iota.campusX.Feature.Society.data.local.UserCacheEntity).\n"
                  + " Expected:\n" + _infoUserCache + "\n"
                  + " Found:\n" + _existingUserCache);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "26254704b20aca1a6a298f32d895e3a9", "87225f6ca40f0053eaff12c722dbec19");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "communities","society_messages","user_cache");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `communities`");
      _db.execSQL("DELETE FROM `society_messages`");
      _db.execSQL("DELETE FROM `user_cache`");
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
    _typeConvertersMap.put(CommunityDao.class, CommunityDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserCacheDao.class, UserCacheDao_Impl.getRequiredConverters());
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
  public CommunityDao communityDao() {
    if (_communityDao != null) {
      return _communityDao;
    } else {
      synchronized(this) {
        if(_communityDao == null) {
          _communityDao = new CommunityDao_Impl(this);
        }
        return _communityDao;
      }
    }
  }

  @Override
  public UserCacheDao userCacheDao() {
    if (_userCacheDao != null) {
      return _userCacheDao;
    } else {
      synchronized(this) {
        if(_userCacheDao == null) {
          _userCacheDao = new UserCacheDao_Impl(this);
        }
        return _userCacheDao;
      }
    }
  }
}
