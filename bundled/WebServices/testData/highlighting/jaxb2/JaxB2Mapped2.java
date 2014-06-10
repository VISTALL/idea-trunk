import javax.xml.bind.annotation.XmlType;

@XmlType
public class JaxB2Mapped2 {
  private short zip;

  public JaxB2Mapped2() {}

  public JaxB2Mapped2(short _zip) {
    zip = _zip;
  }

  public short getZip() { return zip; }
  public void setZip(short _zip) { zip = _zip; }
}