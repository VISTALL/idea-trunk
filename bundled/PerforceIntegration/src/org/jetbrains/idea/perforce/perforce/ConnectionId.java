package org.jetbrains.idea.perforce.perforce;

import com.intellij.util.xmlb.annotations.Tag;

import java.io.DataOutput;
import java.io.IOException;
import java.io.DataInput;

@Tag("ID")
public class ConnectionId {

  public boolean myDoNotUseP4Config;
  public String myP4ConfigFileName;
  public String myWorkingDir;


  public ConnectionId(final String p4ConfigFileName, final String workingDir) {
    myP4ConfigFileName = p4ConfigFileName;
    myWorkingDir = workingDir;
    myDoNotUseP4Config = false;
  }

  public ConnectionId() {
    myDoNotUseP4Config = true;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ConnectionId that = (ConnectionId)o;

    if (myDoNotUseP4Config) {
      return that.myDoNotUseP4Config;
    } else {
      if (myP4ConfigFileName != null ? !myP4ConfigFileName.equals(that.myP4ConfigFileName) : that.myP4ConfigFileName != null) return false;
      return !(myWorkingDir != null ? !myWorkingDir.equals(that.myWorkingDir) : that.myWorkingDir != null);
    }


  }

  public int hashCode() {
    if (myDoNotUseP4Config) {
      return 0;
    } else {
      int result = 0;
      result = 29 * result + (myP4ConfigFileName != null ? myP4ConfigFileName.hashCode() : 0);
      result = 29 * result + (myWorkingDir != null ? myWorkingDir.hashCode() : 0);
      return result;
    }
  }

  public void writeToStream(final DataOutput stream) throws IOException {
    stream.writeByte(myDoNotUseP4Config ? 0 : 1);
    if (!myDoNotUseP4Config) {
      stream.writeUTF(myP4ConfigFileName);
      stream.writeUTF(myWorkingDir);
    }
  }

  public static ConnectionId readFromStream(final DataInput stream) throws IOException {
    byte useP4Config = stream.readByte();
    if (useP4Config == 0) {
      return new ConnectionId();
    }
    String configFileName = stream.readUTF();
    String workingDir = stream.readUTF();
    return new ConnectionId(configFileName, workingDir);
  }
}
