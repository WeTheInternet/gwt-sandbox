package java.lang;

/**
 * (Very) basic emulation for threadgroups.
 */
public class ThreadGroup {
    private String name;
    private ThreadGroup parent;
    private int maxPriority;

    public ThreadGroup(String name) {
        this(null, name);
    }

    public ThreadGroup(ThreadGroup parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public final int getMaxPriority() {
  return maxPriority;
    }

    public final void setName(String name) {
  this.name = name;
    }

    public final String getName() {
  return String.valueOf(name);
    }
}
