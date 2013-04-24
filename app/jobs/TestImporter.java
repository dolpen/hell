package jobs;

import models.Character;
import models.*;
import play.jobs.Job;

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
        Character.makeNewCharacter("カレン");
        Character.makeNewCharacter("フラン");
        Character.makeNewCharacter("リディア");
        Character.makeNewCharacter("エリカ");

        User.deleteAll();
    }
}
