package com.sixrr.xrp.base;

import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;

public abstract class BaseUsageViewDescriptor implements UsageViewDescriptor {
    public static final String REFERENCE_WORD = "reference";
    private final UsageInfo[] usages;

    protected BaseUsageViewDescriptor(UsageInfo[] usages) {
        super();
        this.usages = usages;
    }


    public UsageInfo[] getUsages(){
        return usages;
    }

    public boolean isSearchInText(){
        return false;
    }

    public boolean toMarkInvalidOrReadonlyUsages(){
        return true;
    }

    public String getCodeReferencesWord(){
        return REFERENCE_WORD;
    }

    public String getCommentReferencesWord(){
        return null;
    }

    public boolean cancelAvailable(){
        return true;
    }

    public void refresh(PsiElement[] elements){

    }

    public String getCommentReferencesText(int usagesCount, int filesCount){
        return null;
    }

    public boolean isCancelInCommonGroup(){
        return false;
    }

    public boolean canRefresh(){
        return false;
    }

    public boolean willUsageBeChanged(UsageInfo usageInfo){
        return true;
    }

    public String getHelpID(){
        return null;
    }

    public boolean canFilterMethods(){
        return true;
    }
}
