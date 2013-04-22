package jobs;

import play.Play;
import play.Play.Mode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

    /**
     * {@inheritDoc}
     */
    @Override
    public void doJob() throws Exception {
        super.doJob();

        // テスト用データの生成
        if (Play.mode == Mode.DEV) {
            new TestImporter().now();
        }
    }
}
