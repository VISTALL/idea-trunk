package org.jetbrains.plugins.groovy.mvc.projectView;

import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Krasilschikov
 */
public interface MvcNodeDescriptor {
  @NotNull
  SortInfo getSortInformation();

  enum SortInfo {
    //Common
    DEFAULT_NODE(4545),

    FOLDER(0, 2), // folders first
    FILE(0, 15),
    CLASS(0, 5),
    METHOD(0, 10),
    FIELD(0, 12),

    //TOP Level
    DOMAIN_CLASSES_FOLDER(20),
    CONTROLLERS_FOLDER(30),
    VIEWS_FOLDER(40),
    SERVICES_FOLDER(50),
    CONFIG_FOLDER(60),
    WEB_APP_FOLDER(65),
    SRC_FOLDERS(70),
    TESTS_FOLDER(80),
    TAGLIB_FOLDER(90),

    //Controllers
    CONTROLLER(CLASS.getWeight(), CLASS.getTypeSortWeight(false), 3),

    ACTION(METHOD.getWeight(), METHOD.getTypeSortWeight(false), 8), // if by type - actions will be before methods
    TEST_METHOD(METHOD.getWeight(), METHOD.getTypeSortWeight(false), 8), // if by type - actions will be before methods
    VIEW(0, 10),

    DOMAIN_CLASS(CLASS.getWeight(), CLASS.getTypeSortWeight(false), 4);

    private final int myWeight;
    private final int myTypeSortWeight_Common;
    private final int myTypeSortWeight_ByType;

    SortInfo(final int weight,
             final int typeSortWeight_Common,
             final int typeSortWeight_ByType) {
      myWeight = weight;
      myTypeSortWeight_Common = typeSortWeight_Common;
      myTypeSortWeight_ByType = typeSortWeight_ByType;
    }

    SortInfo(final int weight,
             final int typeSortWeight) {
      this(weight, typeSortWeight, typeSortWeight);
    }

    SortInfo(final int weight) {
      this(weight, 0);
    }

    public int getWeight() {
      return myWeight;
    }

    public int getTypeSortWeight(final boolean sortByType) {
      return sortByType ? myTypeSortWeight_ByType : myTypeSortWeight_Common;
    }
  }
}