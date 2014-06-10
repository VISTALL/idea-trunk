package com.intellij.velocity.psi.formatter;

/**
 * @author Alexey Chmutov
 *         Date: Jul 6, 2009
 *         Time: 8:29:23 PM
 */
interface BlockWithParent {
  BlockWithParent getParent();
  void setParent(BlockWithParent newParent);
}
