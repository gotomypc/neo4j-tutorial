package org.neo4j.tutorial;

import static org.junit.Assert.assertThat;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificActors.containsOnlyActors;
import static org.neo4j.tutorial.matchers.ContainsSpecificNumberOfNodes.containsNumberOfNodes;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

/**
 * In this Koan we start using the new traversal framework to find interesting
 * information from the graph about the Doctor's past life.
 */
public class Koan07
{

    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception
    {
        universe = new EmbeddedDoctorWhoUniverse( new DoctorWhoUniverseGenerator() );
    }

    @AfterClass
    public static void closeTheDatabase()
    {
        universe.stop();
    }

    @Test
    public void shouldDiscoverHowManyIncarnationsOfTheDoctorThereHaveBeen() throws Exception
    {
        Node theDoctor = universe.theDoctor();
        TraversalDescription REGENERATED_ACTORS = null;

        // YOUR CODE GOES HERE
        // SNIPPET_START

        REGENERATED_ACTORS = Traversal.description()
                .relationships( DoctorWhoUniverse.PLAYED, Direction.INCOMING )
                .breadthFirst()
                .evaluator( new Evaluator()
                {
                    public Evaluation evaluate( Path path )
                    {
                        if ( path.endNode()
                                .hasRelationship( DoctorWhoUniverse.REGENERATED_TO, Direction.BOTH ) )
                        {
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        }
                        else
                        {
                            return Evaluation.EXCLUDE_AND_PRUNE;
                        }
                    }
                } );

        // SNIPPET_END

        assertThat( REGENERATED_ACTORS.traverse( theDoctor )
                .nodes(), containsNumberOfNodes( 11 ) );
    }

    @Test
    public void shouldFindTheFirstDoctor()
    {
        Node theDoctor = universe.theDoctor();
        TraversalDescription FIRST_DOCTOR = null;

        // YOUR CODE GOES HERE
        // SNIPPET_START

        FIRST_DOCTOR = Traversal.description()
                .relationships( DoctorWhoUniverse.PLAYED, Direction.INCOMING )
                .depthFirst()
                .evaluator( new Evaluator()
                {
                    public Evaluation evaluate( Path path )
                    {
                        if ( path.endNode()
                                .hasRelationship( DoctorWhoUniverse.REGENERATED_TO, Direction.INCOMING ) )
                        {
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                        else if ( !path.endNode()
                                .hasRelationship( DoctorWhoUniverse.REGENERATED_TO, Direction.OUTGOING ) )
                        {
                            // Catches Richard Hurdnall who played the William
                            // Hartnell's Doctor in The Five Doctors (William
                            // Hartnell had died by then)
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                        else
                        {
                            return Evaluation.INCLUDE_AND_PRUNE;
                        }
                    }
                } );

        // SNIPPET_END

        assertThat( FIRST_DOCTOR.traverse( theDoctor )
                .nodes(), containsOnlyActors( "William Hartnell" ) );
    }
}
