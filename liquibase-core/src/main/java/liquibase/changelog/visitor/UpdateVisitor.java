package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.logging.LogType;

import java.util.Set;

public class UpdateVisitor implements ChangeSetVisitor {

    private Database database;

    private ChangeExecListener execListener;

    /**
     * @deprecated - please use the constructor with ChangeExecListener, which can be null.
     */
    @Deprecated
    public UpdateVisitor(Database database) {
        this.database = database;
    }
    
    public UpdateVisitor(Database database, ChangeExecListener execListener) {
      this(database);
      this.execListener = execListener;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, ChangeLog changeLog, Database database,
                      Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSet.RunStatus runStatus = this.database.getRunStatus(changeSet);
        Scope.getCurrentScope().getLog(getClass()).fine(LogType.LOG, "Running Changeset:" + changeSet);
        fireWillRun(changeSet, changeLog, database, runStatus);
        ExecType execType = null;
        ObjectQuotingStrategy previousStr = this.database.getObjectQuotingStrategy();
        try {
            execType = changeSet.execute(changeLog, execListener, this.database);
        } catch (MigrationFailedException e) {
            fireRunFailed(changeSet, changeLog, database, e);
            throw e;
        }
        if (!runStatus.equals(ChangeSet.RunStatus.NOT_RAN)) {
            execType = ChangeSet.ExecType.RERAN;
        }
        fireRan(changeSet, changeLog, database, execType);
        // reset object quoting strategy after running changeset
        this.database.setObjectQuotingStrategy(previousStr);
        this.database.markChangeSetExecStatus(changeSet, execType);

        this.database.commit();
    }

    protected void fireRunFailed(ChangeSet changeSet, ChangeLog changeLog, Database database, MigrationFailedException e) {
        if (execListener != null) {
            execListener.runFailed(changeSet, changeLog, database, e);
        }
    }

    protected void fireWillRun(ChangeSet changeSet, ChangeLog changeLog, Database database2, RunStatus runStatus) {
      if (execListener != null) {
        execListener.willRun(changeSet, changeLog, database, runStatus);
      }      
    }

    protected void fireRan(ChangeSet changeSet, ChangeLog changeLog, Database database2, ExecType execType) {
      if (execListener != null) {
        execListener.ran(changeSet, changeLog, database, execType);
      }
    }
}
