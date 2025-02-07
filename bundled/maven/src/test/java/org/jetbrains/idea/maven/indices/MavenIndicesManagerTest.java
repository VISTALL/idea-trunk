package org.jetbrains.idea.maven.indices;

import com.intellij.openapi.util.Pair;

import java.io.File;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MavenIndicesManagerTest extends MavenIndicesTestCase {
  private MavenIndicesTestFixture myIndicesFixture;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myIndicesFixture = new MavenIndicesTestFixture(myDir, myProject);
    myIndicesFixture.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    myIndicesFixture.tearDown();
    super.tearDown();
  }

  public void testEnsuringLocalRepositoryIndex() throws Exception {
    File dir1 = myIndicesFixture.getRepositoryHelper().getTestData("dir/foo");
    File dir2 = myIndicesFixture.getRepositoryHelper().getTestData("dir\\foo");
    File dir3 = myIndicesFixture.getRepositoryHelper().getTestData("dir\\foo\\");
    File dir4 = myIndicesFixture.getRepositoryHelper().getTestData("dir/bar");

    List<MavenIndex> indices1 = myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, dir1, Collections.EMPTY_LIST);
    assertEquals(1, indices1.size());
    assertTrue(myIndicesFixture.getIndicesManager().getIndices().contains(indices1.get(0)));

    assertEquals(indices1, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, dir2, Collections.EMPTY_LIST));
    assertEquals(indices1, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, dir3, Collections.EMPTY_LIST));

    List<MavenIndex> indices2 = myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, dir4, Collections.EMPTY_LIST);
    assertFalse(indices1.get(0).equals(indices2.get(0)));
  }

  public void testEnsuringRemoteRepositoryIndex() throws Exception {
    File local = myIndicesFixture.getRepositoryHelper().getTestData("dir");
    Pair<String, String> remote1 = Pair.create("id1", "http://foo/bar");
    Pair<String, String> remote2 = Pair.create("id1", "  http://foo\\bar\\\\  ");
    Pair<String, String> remote3 = Pair.create("id3", "http://foo\\bar\\baz");
    Pair<String, String> remote4 = Pair.create("id4", "http://foo/bar"); // same url
    Pair<String, String> remote5 = Pair.create("id4", "http://foo/baz"); // same id

    assertEquals(2, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, local, Collections.singleton(remote1)).size());
    assertEquals(2, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, local, asList(remote1, remote2)).size());
    assertEquals(3, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, local, asList(remote1, remote2, remote3)).size());
    assertEquals(4, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, local, asList(remote1, remote2, remote3, remote4)).size());
    assertEquals(5, myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, local, asList(remote1, remote2, remote3, remote4, remote5)).size());
  }

  public void testDefaultArchetypes() throws Exception {
    assertArchetypeExists("org.apache.maven.archetypes:maven-archetype-quickstart:RELEASE");
  }

  public void testIndexedArchetypes() throws Exception {
    myIndicesFixture.getRepositoryHelper().addTestData("archetypes");
    myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, myIndicesFixture.getRepositoryHelper().getTestData("archetypes"),
                                                            Collections.EMPTY_LIST);

    assertArchetypeExists("org.apache.maven.archetypes:maven-archetype-foobar:1.0");
  }

  public void testIndexedArchetypesWithSeveralIndicesAfterReopening() throws Exception {
    myIndicesFixture.getRepositoryHelper().addTestData("archetypes");
    myIndicesFixture.getIndicesManager().ensureIndicesExist(myProject, myIndicesFixture.getRepositoryHelper().getTestData("archetypes"),
                                                            Collections.singleton(Pair.create("id", "foo://bar.baz")));

    assertArchetypeExists("org.apache.maven.archetypes:maven-archetype-foobar:1.0");

    myIndicesFixture.tearDown();
    myIndicesFixture.setUp();

    assertArchetypeExists("org.apache.maven.archetypes:maven-archetype-foobar:1.0");
  }

  public void testAddingArchetypes() throws Exception {
    myIndicesFixture.getIndicesManager().addArchetype(new ArchetypeInfo("myGroup",
                                                                        "myArtifact",
                                                                        "666",
                                                                        null,
                                                                        null));

    assertArchetypeExists("myGroup:myArtifact:666");

    myIndicesFixture.tearDown();
    myIndicesFixture.setUp();

    assertArchetypeExists("myGroup:myArtifact:666");
  }

  private void assertArchetypeExists(String archetypeId) {
    Set<ArchetypeInfo> achetypes = myIndicesFixture.getIndicesManager().getArchetypes();
    List<String> actualNames = new ArrayList<String>();
    for (ArchetypeInfo each : achetypes) {
      actualNames.add(each.groupId + ":" + each.artifactId + ":" + each.version);
    }
    assertTrue(actualNames.toString(), actualNames.contains(archetypeId));
  }
}