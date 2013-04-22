package jobs;

import models.Character;
import models.Member;
import models.Res;
import models.User;
import models.Village;
import play.jobs.Job;
import consts.Constants;

public class TestImporter extends Job {

    /**
     * {@inheritDoc}
     */
    @Override
    public void doJob() throws Exception {
        super.doJob();
        Village.deleteAll();
        Member.deleteAll();
        Res.deleteAll();

        Character.deleteAll();
        Character.makeNewCharacter("アヤメ");
        Character c1 = Character.makeNewCharacter("カレン");
        Character c2 = Character.makeNewCharacter("フラン");
        Character c3 = Character.makeNewCharacter("リディア");
        Character c4 = Character.makeNewCharacter("エリカ");

        User.deleteAll();
        User u1 = User.createNewUser("dolpen", "dolpen");
        User u2 = User.createNewUser("dolpen2", "dolpen2");
        User u3 = User.createNewUser("dolpen3", "dolpen3");
        User u4 = User.createNewUser("dolpen4", "dolpen4");
        User.createNewUser("yamada", "yamada");

        Village v = Village.settle(u1, "お試し村", Constants.FORM_CATTLE, 1, 1, true);
        v.enter(u1, c1);
        v.enter(u2, c2);
        v.enter(u3, c3);
        v.enter(u4, c4);

    }
}
