package object;

/**
 *
 * @author mshao1
 */
public class GTConfiguration extends FilePathConfiguration {
    private boolean withHeader =  false;

    public boolean isWithHeader() {
        return withHeader;
    }

    public void setWithHeader(boolean withHeader) {
        this.withHeader = withHeader;
    }
}
