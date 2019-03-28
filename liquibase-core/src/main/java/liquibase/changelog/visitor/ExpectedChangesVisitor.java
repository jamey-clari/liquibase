package liquibase.changelog.visitor;

import liquibase.changelog.ChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.*;

public class ExpectedChangesVisitor implements ChangeSetVisitor {
    private final LinkedHashSet<RanChangeSet> unexpectedChangeSets;

    public ExpectedChangesVisitor(List<RanChangeSet> ranChangeSetList) {
        this.unexpectedChangeSets = new LinkedHashSet<>(ranChangeSetList);
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, ChangeLog changeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        for (Iterator<RanChangeSet> i = unexpectedChangeSets.iterator(); i.hasNext(); ) {
            RanChangeSet ranChangeSet = i.next();
            if (ranChangeSet.isSameAs(changeSet)) {
                i.remove();
            }
        }
    }

    public Collection<RanChangeSet> getUnexpectedChangeSets() {
        return unexpectedChangeSets;
    }

}
