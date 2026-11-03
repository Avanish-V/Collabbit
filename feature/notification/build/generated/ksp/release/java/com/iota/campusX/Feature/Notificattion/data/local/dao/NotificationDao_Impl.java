package com.iota.campusX.Feature.Notificattion.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.paging.PagingSource;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.paging.LimitOffsetPagingSource;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.iota.campusX.Feature.Notificattion.data.local.converters.NotificationConverters;
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity;
import com.iota.campusX.Feature.Notificattion.domain.model.Upvoter;
import com.iotabuild.campuscircle.Notification.entity.EntityType;
import com.iotabuild.campuscircle.Notification.entity.NotificationType;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class NotificationDao_Impl implements NotificationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NotificationEntity> __insertionAdapterOfNotificationEntity;

  private final NotificationConverters __notificationConverters = new NotificationConverters();

  private final SharedSQLiteStatement __preparedStmtOfMarkRead;

  private final SharedSQLiteStatement __preparedStmtOfMarkAllRead;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfMarkActionDone;

  public NotificationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNotificationEntity = new EntityInsertionAdapter<NotificationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `notifications` (`id`,`senderUid`,`senderName`,`senderProfileUrl`,`type`,`entityType`,`entityId`,`title`,`body`,`note`,`deepLink`,`isRead`,`createdAt`,`isActionDone`,`postTitle`,`postThumbnail`,`upvoters`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NotificationEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSenderUid());
        statement.bindString(3, entity.getSenderName());
        if (entity.getSenderProfileUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSenderProfileUrl());
        }
        final String _tmp = __notificationConverters.fromNotificationType(entity.getType());
        statement.bindString(5, _tmp);
        final String _tmp_1 = __notificationConverters.fromEntityType(entity.getEntityType());
        statement.bindString(6, _tmp_1);
        statement.bindString(7, entity.getEntityId());
        if (entity.getTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getTitle());
        }
        if (entity.getBody() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getBody());
        }
        if (entity.getNote() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNote());
        }
        if (entity.getDeepLink() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDeepLink());
        }
        final int _tmp_2 = entity.isRead() ? 1 : 0;
        statement.bindLong(12, _tmp_2);
        statement.bindLong(13, entity.getCreatedAt());
        final int _tmp_3 = entity.isActionDone() ? 1 : 0;
        statement.bindLong(14, _tmp_3);
        if (entity.getPostTitle() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getPostTitle());
        }
        if (entity.getPostThumbnail() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getPostThumbnail());
        }
        final String _tmp_4 = __notificationConverters.fromUpvoterList(entity.getUpvoters());
        statement.bindString(17, _tmp_4);
      }
    };
    this.__preparedStmtOfMarkRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE notifications\n"
                + "        SET isRead = 1\n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAllRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE notifications\n"
                + "        SET isRead = 1\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM notifications\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notifications WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkActionDone = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notifications SET isActionDone = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final NotificationEntity notification,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNotificationEntity.insert(notification);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<NotificationEntity> notifications,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNotificationEntity.insert(notifications);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markRead(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkRead.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfMarkRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAllRead(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAllRead.acquire();
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
          __preparedStmtOfMarkAllRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
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
          __preparedStmtOfClear.release(_stmt);
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
  public Object delete(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markActionDone(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkActionDone.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfMarkActionDone.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public PagingSource<Integer, NotificationEntity> pagingSource() {
    final String _sql = "\n"
            + "        SELECT *\n"
            + "        FROM notifications\n"
            + "        ORDER BY createdAt DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return new LimitOffsetPagingSource<NotificationEntity>(_statement, __db, "notifications") {
      @Override
      @NonNull
      protected List<NotificationEntity> convertRows(@NonNull final Cursor cursor) {
        final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(cursor, "id");
        final int _cursorIndexOfSenderUid = CursorUtil.getColumnIndexOrThrow(cursor, "senderUid");
        final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(cursor, "senderName");
        final int _cursorIndexOfSenderProfileUrl = CursorUtil.getColumnIndexOrThrow(cursor, "senderProfileUrl");
        final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(cursor, "type");
        final int _cursorIndexOfEntityType = CursorUtil.getColumnIndexOrThrow(cursor, "entityType");
        final int _cursorIndexOfEntityId = CursorUtil.getColumnIndexOrThrow(cursor, "entityId");
        final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(cursor, "title");
        final int _cursorIndexOfBody = CursorUtil.getColumnIndexOrThrow(cursor, "body");
        final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(cursor, "note");
        final int _cursorIndexOfDeepLink = CursorUtil.getColumnIndexOrThrow(cursor, "deepLink");
        final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(cursor, "isRead");
        final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(cursor, "createdAt");
        final int _cursorIndexOfIsActionDone = CursorUtil.getColumnIndexOrThrow(cursor, "isActionDone");
        final int _cursorIndexOfPostTitle = CursorUtil.getColumnIndexOrThrow(cursor, "postTitle");
        final int _cursorIndexOfPostThumbnail = CursorUtil.getColumnIndexOrThrow(cursor, "postThumbnail");
        final int _cursorIndexOfUpvoters = CursorUtil.getColumnIndexOrThrow(cursor, "upvoters");
        final List<NotificationEntity> _result = new ArrayList<NotificationEntity>(cursor.getCount());
        while (cursor.moveToNext()) {
          final NotificationEntity _item;
          final long _tmpId;
          _tmpId = cursor.getLong(_cursorIndexOfId);
          final String _tmpSenderUid;
          _tmpSenderUid = cursor.getString(_cursorIndexOfSenderUid);
          final String _tmpSenderName;
          _tmpSenderName = cursor.getString(_cursorIndexOfSenderName);
          final String _tmpSenderProfileUrl;
          if (cursor.isNull(_cursorIndexOfSenderProfileUrl)) {
            _tmpSenderProfileUrl = null;
          } else {
            _tmpSenderProfileUrl = cursor.getString(_cursorIndexOfSenderProfileUrl);
          }
          final NotificationType _tmpType;
          final String _tmp;
          _tmp = cursor.getString(_cursorIndexOfType);
          _tmpType = __notificationConverters.toNotificationType(_tmp);
          final EntityType _tmpEntityType;
          final String _tmp_1;
          _tmp_1 = cursor.getString(_cursorIndexOfEntityType);
          _tmpEntityType = __notificationConverters.toEntityType(_tmp_1);
          final String _tmpEntityId;
          _tmpEntityId = cursor.getString(_cursorIndexOfEntityId);
          final String _tmpTitle;
          if (cursor.isNull(_cursorIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = cursor.getString(_cursorIndexOfTitle);
          }
          final String _tmpBody;
          if (cursor.isNull(_cursorIndexOfBody)) {
            _tmpBody = null;
          } else {
            _tmpBody = cursor.getString(_cursorIndexOfBody);
          }
          final String _tmpNote;
          if (cursor.isNull(_cursorIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = cursor.getString(_cursorIndexOfNote);
          }
          final String _tmpDeepLink;
          if (cursor.isNull(_cursorIndexOfDeepLink)) {
            _tmpDeepLink = null;
          } else {
            _tmpDeepLink = cursor.getString(_cursorIndexOfDeepLink);
          }
          final boolean _tmpIsRead;
          final int _tmp_2;
          _tmp_2 = cursor.getInt(_cursorIndexOfIsRead);
          _tmpIsRead = _tmp_2 != 0;
          final long _tmpCreatedAt;
          _tmpCreatedAt = cursor.getLong(_cursorIndexOfCreatedAt);
          final boolean _tmpIsActionDone;
          final int _tmp_3;
          _tmp_3 = cursor.getInt(_cursorIndexOfIsActionDone);
          _tmpIsActionDone = _tmp_3 != 0;
          final String _tmpPostTitle;
          if (cursor.isNull(_cursorIndexOfPostTitle)) {
            _tmpPostTitle = null;
          } else {
            _tmpPostTitle = cursor.getString(_cursorIndexOfPostTitle);
          }
          final String _tmpPostThumbnail;
          if (cursor.isNull(_cursorIndexOfPostThumbnail)) {
            _tmpPostThumbnail = null;
          } else {
            _tmpPostThumbnail = cursor.getString(_cursorIndexOfPostThumbnail);
          }
          final List<Upvoter> _tmpUpvoters;
          final String _tmp_4;
          _tmp_4 = cursor.getString(_cursorIndexOfUpvoters);
          _tmpUpvoters = __notificationConverters.toUpvoterList(_tmp_4);
          _item = new NotificationEntity(_tmpId,_tmpSenderUid,_tmpSenderName,_tmpSenderProfileUrl,_tmpType,_tmpEntityType,_tmpEntityId,_tmpTitle,_tmpBody,_tmpNote,_tmpDeepLink,_tmpIsRead,_tmpCreatedAt,_tmpIsActionDone,_tmpPostTitle,_tmpPostThumbnail,_tmpUpvoters);
          _result.add(_item);
        }
        return _result;
      }
    };
  }

  @Override
  public Flow<Long> observeUnreadCount() {
    final String _sql = "\n"
            + "        SELECT COUNT(*)\n"
            + "        FROM notifications\n"
            + "        WHERE isRead = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notifications"}, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
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
  public Object unreadCount(final Continuation<? super Long> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(*)\n"
            + "        FROM notifications\n"
            + "        WHERE isRead = 0\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
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
  public Object exists(final long id, final Continuation<? super Boolean> $completion) {
    final String _sql = "\n"
            + "SELECT EXISTS(\n"
            + "    SELECT 1\n"
            + "    FROM notifications\n"
            + "    WHERE id = ?\n"
            + ")\n";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
