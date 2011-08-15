package org.neo4j.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.neo4j.tutorial.matchers.ContainsOnlyHumanCompanions.containsOnlyHumanCompanions;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificTitles.containsOnlyTitles;

import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

/**
 * In this Koan we start to mix indexing and core API to perform more targeted
 * graph operations. We'll mix indexes and core graph operations to explore the
 * Doctor's universe.
 */
public class Koan05 {

    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception {
        universe = new EmbeddedDoctorWhoUniverse(new DoctorWhoUniverseGenerator());
    }
    
    @AfterClass
    public static void closeTheDatabase() {
        universe.stop();
    }

    @Test
    public void shouldCountTheNumberOfDoctorsRegenerations() {

        Index<Node> actorsIndex = universe.getDatabase().index().forNodes("actors");
        int numberOfRegenerations = 1;

        // YOUR CODE GOES HERE
        // SNIPPET_START
        Node firstDoctor = actorsIndex.get("actor", "William Hartnell").getSingle();

        Relationship regeneratedTo = firstDoctor.getSingleRelationship(DoctorWhoUniverseGenerator.REGENERATED_TO, Direction.OUTGOING);

        while (regeneratedTo != null) {
            numberOfRegenerations++;
            regeneratedTo = regeneratedTo.getEndNode().getSingleRelationship(DoctorWhoUniverseGenerator.REGENERATED_TO, Direction.OUTGOING);
        }

        // SNIPPET_END

        assertEquals(11, numberOfRegenerations);
    }

    @Test
    public void shouldFindHumanCompanionsUsingCoreApi() {
        HashSet<Node> humanCompanions = new HashSet<Node>();

        // YOUR CODE GOES HERE
        // SNIPPET_START
        
        Node human = universe.getDatabase().index().forNodes("species").get("species", "Human").getSingle();
        
        Iterable<Relationship> relationships = universe.theDoctor().getRelationships(Direction.INCOMING, DoctorWhoUniverseGenerator.COMPANION_OF);
        for(Relationship rel : relationships) {
            Node companionNode = rel.getStartNode();
            if(companionNode.hasRelationship(Direction.OUTGOING, DoctorWhoUniverseGenerator.IS_A)) {
                Relationship singleRelationship = companionNode.getSingleRelationship(DoctorWhoUniverseGenerator.IS_A, Direction.OUTGOING);
                Node endNode = singleRelationship.getEndNode();
                if(endNode.equals(human)) {
                    humanCompanions.add(companionNode);
                }
            }
        }
        
        // SNIPPET_END

        int numberOfKnownHumanCompanions = 36;
        assertEquals(numberOfKnownHumanCompanions, humanCompanions.size());
        assertThat(humanCompanions, containsOnlyHumanCompanions());
    }

    @Test
    public void shouldFindAllEpisodesWhereRoseTylerFoughtTheDaleks() {
        Index<Node> friendliesIndex = universe.getDatabase().index().forNodes("characters");
        Index<Node> speciesIndex = universe.getDatabase().index().forNodes("species");
        HashSet<Node> episodesWhereRoseFightsTheDaleks = new HashSet<Node>();

        // YOUR CODE GOES HERE
        // SNIPPET_START

        Node roseTyler = friendliesIndex.get("name", "Rose Tyler").getSingle();
        Node daleks = speciesIndex.get("species", "Dalek").getSingle();

        for (Relationship r1 : roseTyler.getRelationships(DoctorWhoUniverseGenerator.APPEARED_IN, Direction.OUTGOING)) {
            Node episode = r1.getEndNode();

            for (Relationship r2 : episode.getRelationships(DoctorWhoUniverseGenerator.APPEARED_IN, Direction.INCOMING)) {
                if (r2.getStartNode().equals(daleks)) {
                    episodesWhereRoseFightsTheDaleks.add(episode);
                }
            }
        }

        // SNIPPET_END

        assertThat(episodesWhereRoseFightsTheDaleks,
                containsOnlyTitles("Army of Ghosts", "The Stolen Earth", "Doomsday", "Journey's End", "Bad Wolf", "The Parting of the Ways", "Dalek"));
    }
}
