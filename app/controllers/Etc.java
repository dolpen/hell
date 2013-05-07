package controllers;

import models.*;
import play.db.jpa.Transactional;
import play.mvc.Controller;

public class Etc extends Controller {

    public static final String[] names = new String[]{
            "くノ一 アヤメ",
            "継母 ベアトリス",
            "金貸し ブラウン",
            "傭兵 カルロス",
            "占星術師 クローディア",
            "煙突掃除屋 ディーノ",
            "司祭 エドガー",
            "学生 エリカ",
            "兄想い フラン",
            "自警団員 ガウェイン",
            "召使い ジョエル",
            "少女 カレン",
            "かぶき者 ケイジ",
            "娼婦 ルーシー",
            "烏賊 ラス",
            "花売り リディア",
            "好青年 マイルズ",
            "踊り子 ミュウ",
            "家庭教師 ナターシャ",
            "少年 ネロ",
            "変人 ノブ",
            "みなしご オーフェン",
            "炭鉱夫 ランディ",
            "学生 レッグ",
            "お嬢様 ロザリー",
            "旅人 シャロン",
            "猟師 スティーヴ",
            "産婆 スージー"
    };

    @Transactional
    public static void reset() {
        if(Village.count()>0)notFound();
        Village.deleteAll();
        Member.deleteAll();
        Res.deleteAll();
        Chara.deleteAll();
        Party.deleteAll();
        User.deleteAll();

        Party p = Party.makeNewParty("ゅゅ", "lyulyu");

        long in = 0L;
        for (String s : names) {
            Chara.makeNewCharacter(p, in, s);
            in++;
        }
        Application.index();
    }


}
