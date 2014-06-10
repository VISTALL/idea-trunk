package org.jetbrains.android.dom.manifest;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.*;
import org.jetbrains.android.dom.AndroidAttributeValue;
import org.jetbrains.android.dom.converters.PackageClassConverter;

/**
 * @author yole
 */
public interface Receiver extends ApplicationComponent {
    @Attribute("name")
    @Required
    @Convert(PackageClassConverter.class)
    @ExtendClass("android.content.BroadcastReceiver")
    AndroidAttributeValue<PsiClass> getReceiverClass();
}