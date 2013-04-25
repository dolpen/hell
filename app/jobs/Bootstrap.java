package jobs;

import models.Character;
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
    }
}
