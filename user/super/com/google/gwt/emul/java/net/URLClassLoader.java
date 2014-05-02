package java.net;

/**
 * This stripped down URLClassLoader class is simply here to give us cross-platform
 * support for code that might need a valid classloader.
 * 
 * If support is ever needed, we can implement a generator which will set the URLs 
 * of any URLClassLoader to the jars and source paths used to compile GWT.
 * 
 * @author "James X. Nelson (james@wetheinter.net)"
 *
 */
public class URLClassLoader extends ClassLoader {

  private URL[] urls;
  
  public URLClassLoader(URL[] urls, ClassLoader parent) {
    super(parent);
    this.urls = urls;
  }

  public URLClassLoader(URL[] urls) {
    this(urls, ClassLoader.getSystemClassLoader());
  }

  // Included here so attempts at reflection succeed
  protected void addURL(URL url) {
    URL[] newUrls = new URL[urls.length+1];
    System.arraycopy(url, 0, newUrls, 0, urls.length);
    newUrls[urls.length] = url;
    urls = newUrls;
  }
  
  public URL[] getURLs() {
    return urls;
  }

  public static URLClassLoader newInstance(final URL[] urls, final ClassLoader parent) {
      return new URLClassLoader(urls, parent);
  }

  public static URLClassLoader newInstance(final URL[] urls) {
    return new URLClassLoader(urls);
  }
  
}
