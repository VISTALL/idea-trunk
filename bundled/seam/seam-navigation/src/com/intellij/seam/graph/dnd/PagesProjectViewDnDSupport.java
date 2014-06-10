package com.intellij.seam.graph.dnd;

import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.jsf.yfilesGraph.dnd.BasicWebFacetProjectViewDnDSupport;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.graph.beans.BasicPagesEdge;
import com.intellij.seam.graph.beans.BasicPagesNode;
import com.intellij.seam.graph.beans.PageNode;
import com.intellij.seam.model.xml.PagesDomModelManager;
import com.intellij.seam.model.xml.PagesModel;
import com.intellij.seam.model.xml.pages.Page;
import com.intellij.seam.model.xml.pages.Pages;
import com.intellij.util.Function;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class PagesProjectViewDnDSupport extends BasicWebFacetProjectViewDnDSupport<BasicPagesNode, BasicPagesEdge> {
  private final XmlFile myXmlFile;

  public PagesProjectViewDnDSupport(final XmlFile xmlFile,
                                    GraphBuilder<BasicPagesNode, BasicPagesEdge> builder,
                                    @NotNull final WebFacet facetScope) {
    super(builder, facetScope);
    myXmlFile = xmlFile;
  }


  private static Function<Pages, PageNode> getPageNodeFunction(@NotNull final String webPath) {
    return new Function<Pages, PageNode>() {
      public PageNode fun(final Pages pages) {
        final Page page = pages.addPage();
        page.getViewId().setStringValue(webPath);
        return new PageNode((Page)page.createStableCopy(), webPath);
      }
    };
  }

  private static PageNode startInWCA(final Pages pages, final Function<Pages, PageNode> function) {
    return new WriteCommandAction<PageNode>(pages.getManager().getProject(), DomUtil.getFile(pages)) {
      protected void run(final Result<PageNode> result) throws Throwable {
        result.setResult(function.fun(pages));
      }
    }.execute().getResultObject();

  }

  @Nullable
  private Pages getPages() {
    final PagesModel pagesModel = PagesDomModelManager.getInstance(myXmlFile.getProject()).getPagesModel(myXmlFile);
    if (pagesModel != null) {
      return pagesModel.getRoots().get(0).getRootElement();
    }
    return null;
  }

  @Nullable
  protected BasicPagesNode createNodeObject(@NotNull final String webPath) {
    final Pages pages = getPages();

    return pages != null ? startInWCA(pages, getPageNodeFunction(webPath)) : null;
  }

  protected boolean areNodesEquals(@NotNull final String webPath, @NotNull final BasicPagesNode nodeObject) {
    return webPath.equals(nodeObject.getName());
  }
}

