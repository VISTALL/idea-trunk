package com.sixrr.xrp.base;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;

import java.util.Comparator;

public class UsageInfoContainmentComparator
        implements Comparator<UsageInfo>{
    public int compare(UsageInfo o1, UsageInfo o2){
        final PsiElement element1 = o1.getElement();
        final PsiElement element2 = o2.getElement();
        assert element1 != null;
        assert element2 != null;
        final PsiFile containingFile1 = element1.getContainingFile();
        final PsiFile containingFile2 = element2.getContainingFile();
        if(!containingFile1.equals(containingFile2)){
            final String path1 = getPathForPsiFile(containingFile1);
            final String path2 = getPathForPsiFile(containingFile2);
            return path1.compareTo(path2);
        }
        final TextRange range1 = element1.getTextRange();
        final TextRange range2 = element2.getTextRange();
        if(range1.contains(range2)){
            return 1;
        }
        if(range2.contains(range1)){
            return -1;
        }
        final int start1 = range1.getStartOffset();
        final int start2 = range2.getStartOffset();
        if(start1 < start2){
            return -1;
        } else{
            return 1;
        }
    }

    private static String getPathForPsiFile(PsiFile file){
        final VirtualFile virtualFile = file.getVirtualFile();
        if(virtualFile == null){
            return null;
        }
        return virtualFile.getPath();
    }
}
