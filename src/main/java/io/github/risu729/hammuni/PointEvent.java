/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
@AllArgsConstructor
enum PointEvent {

  // リアクションの付与
  REACTION(3, 10),

  // 意見募集チャンネルでのリアクション付与
  SURVEY_CHANNEL_REACTION(3),

  // アンケートへのリアクション付与
  // アンケートかどうかはロール(@everyone含む)へのメンションが含まれているかで判定
  SURVEY_MESSAGE_REACTION(3),

  // 絵文字(Unicode or Server)を含んだメッセージ送信
  EMOJI(5, 5),

  // ファイルの送信
  ATTACHMENT(3, 10),

  // 共有用チャンネルでのファイルの送信
  ATTACHMENT_SHARE(5),

  // URLの送信
  URL(3, 10),

  // 共有用チャンネルでのファイルの送信
  URL_SHARE(5),

  // 意見募集チャンネルでのメッセージ送信
  SURVEY_MESSAGE(5),

  // メッセージ送信
  // n = m (m + 1) / 2 (1 ≤ m ≤ 10, m ∊ ℤ) で表されるn回目のみ
  MESSAGE(5),

  // VCに参加している間、30分ごとに1回付与・最大5回/日
  VC(10),

  // 1日(0:00 JSTにリセット)で初めてのメッセージ送信
  FIRST_MESSAGE(30),

  // 1日で初めてのVCへの参加
  FIRST_VC(30),

  // サーバーへの新規参加
  SERVER_JOIN(100),

  // スレッドの作成 (フォーラムでの作成は除く)
  PUBLIC_THREAD_CREATE(0, 30, true),

  // プライベートスレッドの作成
  PRIVATE_THREAD_CREATE(0, 100, true),

  // 【未実装】名前が金色になるロールの付与
  GOLD_ROLE(0, 5000, true),

  // 【未実装】名前が銀色になるロールの付与
  SILVER_ROLE(0, 3000, true),
  ;

  int gainPoint;
  int consumePoint;
  boolean rejectIfNotEnough;

  PointEvent(int gainPoint) {
    this(gainPoint, 0);
  }

  PointEvent(int gainPoint, int consumePoint) {
    this(gainPoint, consumePoint, false);
  }
}
