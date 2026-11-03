package com.iota.campusX.Feature.Society.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
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
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CommunityDao_Impl implements CommunityDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CommunityEntity> __insertionAdapterOfCommunityEntity;

  private final EntityInsertionAdapter<SocietyMessageEntity> __insertionAdapterOfSocietyMessageEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCommunity;

  private final SharedSQLiteStatement __preparedStmtOfLeaveCommunity;

  private final SharedSQLiteStatement __preparedStmtOfClearUnreadCount;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastMessage;

  private final SharedSQLiteStatement __preparedStmtOfClearAllCommunities;

  private final SharedSQLiteStatement __preparedStmtOfMarkMessagesAsRead;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageProgress;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearAllMessages;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessage;

  private final SharedSQLiteStatement __preparedStmtOfClearMessagesForSociety;

  public CommunityDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCommunityEntity = new EntityInsertionAdapter<CommunityEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `communities` (`id`,`name`,`description`,`logoUrl`,`memberCount`,`category`,`creatorId`,`createdAt`,`isSynced`,`isJoined`,`isDeleted`,`pinnedMessageId`,`pinnedMessageText`,`unreadCount`,`lastMessageText`,`lastMessageTime`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CommunityEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        if (entity.getLogoUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getLogoUrl());
        }
        statement.bindLong(5, entity.getMemberCount());
        statement.bindString(6, entity.getCategory());
        statement.bindString(7, entity.getCreatorId());
        statement.bindLong(8, entity.getCreatedAt());
        final int _tmp = entity.isSynced() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isJoined() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.isDeleted() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        if (entity.getPinnedMessageId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getPinnedMessageId());
        }
        if (entity.getPinnedMessageText() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getPinnedMessageText());
        }
        statement.bindLong(14, entity.getUnreadCount());
        if (entity.getLastMessageText() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getLastMessageText());
        }
        if (entity.getLastMessageTime() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getLastMessageTime());
        }
      }
    };
    this.__insertionAdapterOfSocietyMessageEntity = new EntityInsertionAdapter<SocietyMessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `society_messages` (`id`,`societyId`,`senderId`,`senderName`,`senderAvatarUrl`,`text`,`type`,`mediaUrl`,`thumbnailUrl`,`width`,`height`,`aspectRatio`,`fileName`,`timestamp`,`replyToId`,`replyToText`,`replyToName`,`reactions`,`isRead`,`isSending`,`isFailed`,`uploadProgress`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SocietyMessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSocietyId());
        statement.bindString(3, entity.getSenderId());
        statement.bindString(4, entity.getSenderName());
        if (entity.getSenderAvatarUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getSenderAvatarUrl());
        }
        statement.bindString(6, entity.getText());
        statement.bindString(7, entity.getType());
        if (entity.getMediaUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMediaUrl());
        }
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getThumbnailUrl());
        }
        statement.bindLong(10, entity.getWidth());
        statement.bindLong(11, entity.getHeight());
        statement.bindDouble(12, entity.getAspectRatio());
        if (entity.getFileName() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getFileName());
        }
        statement.bindLong(14, entity.getTimestamp());
        if (entity.getReplyToId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getReplyToId());
        }
        if (entity.getReplyToText() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getReplyToText());
        }
        if (entity.getReplyToName() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getReplyToName());
        }
        final String _tmp = __converters.fromMap(entity.getReactions());
        statement.bindString(18, _tmp);
        final int _tmp_1 = entity.isRead() ? 1 : 0;
        statement.bindLong(19, _tmp_1);
        final int _tmp_2 = entity.isSending() ? 1 : 0;
        statement.bindLong(20, _tmp_2);
        final int _tmp_3 = entity.isFailed() ? 1 : 0;
        statement.bindLong(21, _tmp_3);
        statement.bindDouble(22, entity.getUploadProgress());
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE communities SET isSynced = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCommunity = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM communities WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfLeaveCommunity = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE communities SET isJoined = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearUnreadCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE communities SET unreadCount = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLastMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE communities SET unreadCount = unreadCount + ?, lastMessageText = ?, lastMessageTime = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllCommunities = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM communities";
        return _query;
      }
    };
    this.__preparedStmtOfMarkMessagesAsRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE society_messages SET isRead = 1 WHERE societyId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMessageProgress = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE society_messages SET uploadProgress = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMessageStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE society_messages SET isSending = ?, isFailed = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM society_messages";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM society_messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearMessagesForSociety = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM society_messages WHERE societyId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCommunities(final List<CommunityEntity> communities,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCommunityEntity.insert(communities);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCommunity(final CommunityEntity community,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCommunityEntity.insert(community);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessages(final List<SocietyMessageEntity> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSocietyMessageEntity.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessage(final SocietyMessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSocietyMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfMarkSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCommunity(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCommunity.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteCommunity.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object leaveCommunity(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfLeaveCommunity.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfLeaveCommunity.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearUnreadCount(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearUnreadCount.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfClearUnreadCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastMessage(final String id, final int increment, final String text,
      final long time, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastMessage.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, increment);
        _argIndex = 2;
        _stmt.bindString(_argIndex, text);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 4;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateLastMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllCommunities(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllCommunities.acquire();
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
          __preparedStmtOfClearAllCommunities.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markMessagesAsRead(final String societyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkMessagesAsRead.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, societyId);
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
          __preparedStmtOfMarkMessagesAsRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageProgress(final String messageId, final float progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageProgress.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, progress);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfUpdateMessageProgress.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageStatus(final String messageId, final boolean isSending,
      final boolean isFailed, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isSending ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        final int _tmp_1 = isFailed ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp_1);
        _argIndex = 3;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfUpdateMessageStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllMessages(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllMessages.acquire();
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
          __preparedStmtOfClearAllMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final String messageId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessage.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfDeleteMessage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearMessagesForSociety(final String societyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearMessagesForSociety.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, societyId);
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
          __preparedStmtOfClearMessagesForSociety.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CommunityEntity>> getAllCommunities() {
    final String _sql = "SELECT * FROM communities WHERE isDeleted = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"communities"}, new Callable<List<CommunityEntity>>() {
      @Override
      @NonNull
      public List<CommunityEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUrl");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCreatorId = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfIsJoined = CursorUtil.getColumnIndexOrThrow(_cursor, "isJoined");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfPinnedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageId");
          final int _cursorIndexOfPinnedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageText");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfLastMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageText");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final List<CommunityEntity> _result = new ArrayList<CommunityEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CommunityEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpLogoUrl;
            if (_cursor.isNull(_cursorIndexOfLogoUrl)) {
              _tmpLogoUrl = null;
            } else {
              _tmpLogoUrl = _cursor.getString(_cursorIndexOfLogoUrl);
            }
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpCreatorId;
            _tmpCreatorId = _cursor.getString(_cursorIndexOfCreatorId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp != 0;
            final boolean _tmpIsJoined;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsJoined);
            _tmpIsJoined = _tmp_1 != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_2 != 0;
            final String _tmpPinnedMessageId;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageId)) {
              _tmpPinnedMessageId = null;
            } else {
              _tmpPinnedMessageId = _cursor.getString(_cursorIndexOfPinnedMessageId);
            }
            final String _tmpPinnedMessageText;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageText)) {
              _tmpPinnedMessageText = null;
            } else {
              _tmpPinnedMessageText = _cursor.getString(_cursorIndexOfPinnedMessageText);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final String _tmpLastMessageText;
            if (_cursor.isNull(_cursorIndexOfLastMessageText)) {
              _tmpLastMessageText = null;
            } else {
              _tmpLastMessageText = _cursor.getString(_cursorIndexOfLastMessageText);
            }
            final Long _tmpLastMessageTime;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmpLastMessageTime = null;
            } else {
              _tmpLastMessageTime = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _item = new CommunityEntity(_tmpId,_tmpName,_tmpDescription,_tmpLogoUrl,_tmpMemberCount,_tmpCategory,_tmpCreatorId,_tmpCreatedAt,_tmpIsSynced,_tmpIsJoined,_tmpIsDeleted,_tmpPinnedMessageId,_tmpPinnedMessageText,_tmpUnreadCount,_tmpLastMessageText,_tmpLastMessageTime);
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
  public Flow<List<CommunityEntity>> getJoinedCommunities() {
    final String _sql = "SELECT * FROM communities WHERE isJoined = 1 AND isDeleted = 0 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"communities"}, new Callable<List<CommunityEntity>>() {
      @Override
      @NonNull
      public List<CommunityEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUrl");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCreatorId = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfIsJoined = CursorUtil.getColumnIndexOrThrow(_cursor, "isJoined");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfPinnedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageId");
          final int _cursorIndexOfPinnedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageText");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfLastMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageText");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final List<CommunityEntity> _result = new ArrayList<CommunityEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CommunityEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpLogoUrl;
            if (_cursor.isNull(_cursorIndexOfLogoUrl)) {
              _tmpLogoUrl = null;
            } else {
              _tmpLogoUrl = _cursor.getString(_cursorIndexOfLogoUrl);
            }
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpCreatorId;
            _tmpCreatorId = _cursor.getString(_cursorIndexOfCreatorId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp != 0;
            final boolean _tmpIsJoined;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsJoined);
            _tmpIsJoined = _tmp_1 != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_2 != 0;
            final String _tmpPinnedMessageId;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageId)) {
              _tmpPinnedMessageId = null;
            } else {
              _tmpPinnedMessageId = _cursor.getString(_cursorIndexOfPinnedMessageId);
            }
            final String _tmpPinnedMessageText;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageText)) {
              _tmpPinnedMessageText = null;
            } else {
              _tmpPinnedMessageText = _cursor.getString(_cursorIndexOfPinnedMessageText);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final String _tmpLastMessageText;
            if (_cursor.isNull(_cursorIndexOfLastMessageText)) {
              _tmpLastMessageText = null;
            } else {
              _tmpLastMessageText = _cursor.getString(_cursorIndexOfLastMessageText);
            }
            final Long _tmpLastMessageTime;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmpLastMessageTime = null;
            } else {
              _tmpLastMessageTime = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _item = new CommunityEntity(_tmpId,_tmpName,_tmpDescription,_tmpLogoUrl,_tmpMemberCount,_tmpCategory,_tmpCreatorId,_tmpCreatedAt,_tmpIsSynced,_tmpIsJoined,_tmpIsDeleted,_tmpPinnedMessageId,_tmpPinnedMessageText,_tmpUnreadCount,_tmpLastMessageText,_tmpLastMessageTime);
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
  public Object getCommunityById(final String id,
      final Continuation<? super CommunityEntity> $completion) {
    final String _sql = "SELECT * FROM communities WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CommunityEntity>() {
      @Override
      @Nullable
      public CommunityEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUrl");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCreatorId = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfIsJoined = CursorUtil.getColumnIndexOrThrow(_cursor, "isJoined");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfPinnedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageId");
          final int _cursorIndexOfPinnedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageText");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfLastMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageText");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final CommunityEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpLogoUrl;
            if (_cursor.isNull(_cursorIndexOfLogoUrl)) {
              _tmpLogoUrl = null;
            } else {
              _tmpLogoUrl = _cursor.getString(_cursorIndexOfLogoUrl);
            }
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpCreatorId;
            _tmpCreatorId = _cursor.getString(_cursorIndexOfCreatorId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp != 0;
            final boolean _tmpIsJoined;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsJoined);
            _tmpIsJoined = _tmp_1 != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_2 != 0;
            final String _tmpPinnedMessageId;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageId)) {
              _tmpPinnedMessageId = null;
            } else {
              _tmpPinnedMessageId = _cursor.getString(_cursorIndexOfPinnedMessageId);
            }
            final String _tmpPinnedMessageText;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageText)) {
              _tmpPinnedMessageText = null;
            } else {
              _tmpPinnedMessageText = _cursor.getString(_cursorIndexOfPinnedMessageText);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final String _tmpLastMessageText;
            if (_cursor.isNull(_cursorIndexOfLastMessageText)) {
              _tmpLastMessageText = null;
            } else {
              _tmpLastMessageText = _cursor.getString(_cursorIndexOfLastMessageText);
            }
            final Long _tmpLastMessageTime;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmpLastMessageTime = null;
            } else {
              _tmpLastMessageTime = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _result = new CommunityEntity(_tmpId,_tmpName,_tmpDescription,_tmpLogoUrl,_tmpMemberCount,_tmpCategory,_tmpCreatorId,_tmpCreatedAt,_tmpIsSynced,_tmpIsJoined,_tmpIsDeleted,_tmpPinnedMessageId,_tmpPinnedMessageText,_tmpUnreadCount,_tmpLastMessageText,_tmpLastMessageTime);
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
  public Flow<CommunityEntity> getCommunityByIdFlow(final String id) {
    final String _sql = "SELECT * FROM communities WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"communities"}, new Callable<CommunityEntity>() {
      @Override
      @Nullable
      public CommunityEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUrl");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCreatorId = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfIsJoined = CursorUtil.getColumnIndexOrThrow(_cursor, "isJoined");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfPinnedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageId");
          final int _cursorIndexOfPinnedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageText");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfLastMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageText");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final CommunityEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpLogoUrl;
            if (_cursor.isNull(_cursorIndexOfLogoUrl)) {
              _tmpLogoUrl = null;
            } else {
              _tmpLogoUrl = _cursor.getString(_cursorIndexOfLogoUrl);
            }
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpCreatorId;
            _tmpCreatorId = _cursor.getString(_cursorIndexOfCreatorId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp != 0;
            final boolean _tmpIsJoined;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsJoined);
            _tmpIsJoined = _tmp_1 != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_2 != 0;
            final String _tmpPinnedMessageId;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageId)) {
              _tmpPinnedMessageId = null;
            } else {
              _tmpPinnedMessageId = _cursor.getString(_cursorIndexOfPinnedMessageId);
            }
            final String _tmpPinnedMessageText;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageText)) {
              _tmpPinnedMessageText = null;
            } else {
              _tmpPinnedMessageText = _cursor.getString(_cursorIndexOfPinnedMessageText);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final String _tmpLastMessageText;
            if (_cursor.isNull(_cursorIndexOfLastMessageText)) {
              _tmpLastMessageText = null;
            } else {
              _tmpLastMessageText = _cursor.getString(_cursorIndexOfLastMessageText);
            }
            final Long _tmpLastMessageTime;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmpLastMessageTime = null;
            } else {
              _tmpLastMessageTime = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _result = new CommunityEntity(_tmpId,_tmpName,_tmpDescription,_tmpLogoUrl,_tmpMemberCount,_tmpCategory,_tmpCreatorId,_tmpCreatedAt,_tmpIsSynced,_tmpIsJoined,_tmpIsDeleted,_tmpPinnedMessageId,_tmpPinnedMessageText,_tmpUnreadCount,_tmpLastMessageText,_tmpLastMessageTime);
          } else {
            _result = null;
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
  public Object getUnsyncedCommunities(
      final Continuation<? super List<CommunityEntity>> $completion) {
    final String _sql = "SELECT * FROM communities WHERE isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CommunityEntity>>() {
      @Override
      @NonNull
      public List<CommunityEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUrl");
          final int _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfCreatorId = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfIsJoined = CursorUtil.getColumnIndexOrThrow(_cursor, "isJoined");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfPinnedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageId");
          final int _cursorIndexOfPinnedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "pinnedMessageText");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfLastMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageText");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final List<CommunityEntity> _result = new ArrayList<CommunityEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CommunityEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpLogoUrl;
            if (_cursor.isNull(_cursorIndexOfLogoUrl)) {
              _tmpLogoUrl = null;
            } else {
              _tmpLogoUrl = _cursor.getString(_cursorIndexOfLogoUrl);
            }
            final int _tmpMemberCount;
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final String _tmpCreatorId;
            _tmpCreatorId = _cursor.getString(_cursorIndexOfCreatorId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp != 0;
            final boolean _tmpIsJoined;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsJoined);
            _tmpIsJoined = _tmp_1 != 0;
            final boolean _tmpIsDeleted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp_2 != 0;
            final String _tmpPinnedMessageId;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageId)) {
              _tmpPinnedMessageId = null;
            } else {
              _tmpPinnedMessageId = _cursor.getString(_cursorIndexOfPinnedMessageId);
            }
            final String _tmpPinnedMessageText;
            if (_cursor.isNull(_cursorIndexOfPinnedMessageText)) {
              _tmpPinnedMessageText = null;
            } else {
              _tmpPinnedMessageText = _cursor.getString(_cursorIndexOfPinnedMessageText);
            }
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final String _tmpLastMessageText;
            if (_cursor.isNull(_cursorIndexOfLastMessageText)) {
              _tmpLastMessageText = null;
            } else {
              _tmpLastMessageText = _cursor.getString(_cursorIndexOfLastMessageText);
            }
            final Long _tmpLastMessageTime;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmpLastMessageTime = null;
            } else {
              _tmpLastMessageTime = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _item = new CommunityEntity(_tmpId,_tmpName,_tmpDescription,_tmpLogoUrl,_tmpMemberCount,_tmpCategory,_tmpCreatorId,_tmpCreatedAt,_tmpIsSynced,_tmpIsJoined,_tmpIsDeleted,_tmpPinnedMessageId,_tmpPinnedMessageText,_tmpUnreadCount,_tmpLastMessageText,_tmpLastMessageTime);
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
  public Object getJoinedCommunityIds(final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT id FROM communities WHERE isJoined = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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
  public Flow<List<SocietyMessageEntity>> getMessagesForSociety(final String societyId) {
    final String _sql = "SELECT * FROM society_messages WHERE societyId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, societyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"society_messages"}, new Callable<List<SocietyMessageEntity>>() {
      @Override
      @NonNull
      public List<SocietyMessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSocietyId = CursorUtil.getColumnIndexOrThrow(_cursor, "societyId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfSenderAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "senderAvatarUrl");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfAspectRatio = CursorUtil.getColumnIndexOrThrow(_cursor, "aspectRatio");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfReplyToId = CursorUtil.getColumnIndexOrThrow(_cursor, "replyToId");
          final int _cursorIndexOfReplyToText = CursorUtil.getColumnIndexOrThrow(_cursor, "replyToText");
          final int _cursorIndexOfReplyToName = CursorUtil.getColumnIndexOrThrow(_cursor, "replyToName");
          final int _cursorIndexOfReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "reactions");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfIsSending = CursorUtil.getColumnIndexOrThrow(_cursor, "isSending");
          final int _cursorIndexOfIsFailed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFailed");
          final int _cursorIndexOfUploadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadProgress");
          final List<SocietyMessageEntity> _result = new ArrayList<SocietyMessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SocietyMessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSocietyId;
            _tmpSocietyId = _cursor.getString(_cursorIndexOfSocietyId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpSenderAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfSenderAvatarUrl)) {
              _tmpSenderAvatarUrl = null;
            } else {
              _tmpSenderAvatarUrl = _cursor.getString(_cursorIndexOfSenderAvatarUrl);
            }
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final float _tmpAspectRatio;
            _tmpAspectRatio = _cursor.getFloat(_cursorIndexOfAspectRatio);
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpReplyToId;
            if (_cursor.isNull(_cursorIndexOfReplyToId)) {
              _tmpReplyToId = null;
            } else {
              _tmpReplyToId = _cursor.getString(_cursorIndexOfReplyToId);
            }
            final String _tmpReplyToText;
            if (_cursor.isNull(_cursorIndexOfReplyToText)) {
              _tmpReplyToText = null;
            } else {
              _tmpReplyToText = _cursor.getString(_cursorIndexOfReplyToText);
            }
            final String _tmpReplyToName;
            if (_cursor.isNull(_cursorIndexOfReplyToName)) {
              _tmpReplyToName = null;
            } else {
              _tmpReplyToName = _cursor.getString(_cursorIndexOfReplyToName);
            }
            final Map<String, List<String>> _tmpReactions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfReactions);
            _tmpReactions = __converters.toMap(_tmp);
            final boolean _tmpIsRead;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_1 != 0;
            final boolean _tmpIsSending;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSending);
            _tmpIsSending = _tmp_2 != 0;
            final boolean _tmpIsFailed;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsFailed);
            _tmpIsFailed = _tmp_3 != 0;
            final float _tmpUploadProgress;
            _tmpUploadProgress = _cursor.getFloat(_cursorIndexOfUploadProgress);
            _item = new SocietyMessageEntity(_tmpId,_tmpSocietyId,_tmpSenderId,_tmpSenderName,_tmpSenderAvatarUrl,_tmpText,_tmpType,_tmpMediaUrl,_tmpThumbnailUrl,_tmpWidth,_tmpHeight,_tmpAspectRatio,_tmpFileName,_tmpTimestamp,_tmpReplyToId,_tmpReplyToText,_tmpReplyToName,_tmpReactions,_tmpIsRead,_tmpIsSending,_tmpIsFailed,_tmpUploadProgress);
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
  public Object getMessageById(final String id,
      final Continuation<? super SocietyMessageEntity> $completion) {
    final String _sql = "SELECT * FROM society_messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SocietyMessageEntity>() {
      @Override
      @Nullable
      public SocietyMessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSocietyId = CursorUtil.getColumnIndexOrThrow(_cursor, "societyId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfSenderAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "senderAvatarUrl");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnailUrl");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfAspectRatio = CursorUtil.getColumnIndexOrThrow(_cursor, "aspectRatio");
          final int _cursorIndexOfFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "fileName");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfReplyToId = CursorUtil.getColumnIndexOrThrow(_cursor, "replyToId");
          final int _cursorIndexOfReplyToText = CursorUtil.getColumnIndexOrThrow(_cursor, "replyToText");
          final int _cursorIndexOfReplyToName = CursorUtil.getColumnIndexOrThrow(_cursor, "replyToName");
          final int _cursorIndexOfReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "reactions");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfIsSending = CursorUtil.getColumnIndexOrThrow(_cursor, "isSending");
          final int _cursorIndexOfIsFailed = CursorUtil.getColumnIndexOrThrow(_cursor, "isFailed");
          final int _cursorIndexOfUploadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadProgress");
          final SocietyMessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSocietyId;
            _tmpSocietyId = _cursor.getString(_cursorIndexOfSocietyId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpSenderAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfSenderAvatarUrl)) {
              _tmpSenderAvatarUrl = null;
            } else {
              _tmpSenderAvatarUrl = _cursor.getString(_cursorIndexOfSenderAvatarUrl);
            }
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            if (_cursor.isNull(_cursorIndexOfMediaUrl)) {
              _tmpMediaUrl = null;
            } else {
              _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final float _tmpAspectRatio;
            _tmpAspectRatio = _cursor.getFloat(_cursorIndexOfAspectRatio);
            final String _tmpFileName;
            if (_cursor.isNull(_cursorIndexOfFileName)) {
              _tmpFileName = null;
            } else {
              _tmpFileName = _cursor.getString(_cursorIndexOfFileName);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpReplyToId;
            if (_cursor.isNull(_cursorIndexOfReplyToId)) {
              _tmpReplyToId = null;
            } else {
              _tmpReplyToId = _cursor.getString(_cursorIndexOfReplyToId);
            }
            final String _tmpReplyToText;
            if (_cursor.isNull(_cursorIndexOfReplyToText)) {
              _tmpReplyToText = null;
            } else {
              _tmpReplyToText = _cursor.getString(_cursorIndexOfReplyToText);
            }
            final String _tmpReplyToName;
            if (_cursor.isNull(_cursorIndexOfReplyToName)) {
              _tmpReplyToName = null;
            } else {
              _tmpReplyToName = _cursor.getString(_cursorIndexOfReplyToName);
            }
            final Map<String, List<String>> _tmpReactions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfReactions);
            _tmpReactions = __converters.toMap(_tmp);
            final boolean _tmpIsRead;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_1 != 0;
            final boolean _tmpIsSending;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSending);
            _tmpIsSending = _tmp_2 != 0;
            final boolean _tmpIsFailed;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsFailed);
            _tmpIsFailed = _tmp_3 != 0;
            final float _tmpUploadProgress;
            _tmpUploadProgress = _cursor.getFloat(_cursorIndexOfUploadProgress);
            _result = new SocietyMessageEntity(_tmpId,_tmpSocietyId,_tmpSenderId,_tmpSenderName,_tmpSenderAvatarUrl,_tmpText,_tmpType,_tmpMediaUrl,_tmpThumbnailUrl,_tmpWidth,_tmpHeight,_tmpAspectRatio,_tmpFileName,_tmpTimestamp,_tmpReplyToId,_tmpReplyToText,_tmpReplyToName,_tmpReactions,_tmpIsRead,_tmpIsSending,_tmpIsFailed,_tmpUploadProgress);
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
  public Object deleteCommunitiesNotIn(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM communities WHERE id NOT IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
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

  @Override
  public Object deleteMessagesForSocietiesNotIn(final List<String> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM society_messages WHERE societyId NOT IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
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
