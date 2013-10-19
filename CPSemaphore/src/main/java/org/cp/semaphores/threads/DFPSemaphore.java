package org.cp.semaphores.threads;

import com.google.common.base.Objects;
import org.apache.log4j.Logger;
import org.cp.semaphores.fractal.path.FractalPath;
import org.cp.semaphores.tree.FractalDrawable;

import java.util.Deque;

/**
 * @author kornicameister
 * @version 0.0.1
 * @since 0.0.1
 */
public class DFPSemaphore
        extends FSemaphore {
    private static final Logger LOGGER = Logger.getLogger(DFPSemaphore.class);
    private final FractalDrawable fractalDrawable;
    private FractalPath lastPath = new FractalPath();

    public DFPSemaphore(final Deque<FractalPath> resource, final FractalDrawable fractalDrawable) {
        super(resource);
        this.fractalDrawable = fractalDrawable;
    }

    @Override
    protected void runInternal() {
        LOGGER.info(String.format("Entering critical => %d", this.rounds.getRound()));
        while (this.resource.size() > 0) {
            final FractalPath path = this.resource.pollLast();
            if (this.lastPath != null && !this.lastPath.equals(path)) {
                LOGGER.debug(String.format("Polled value=%s", path));
                this.fractalDrawable.updateDrawing(path);
                this.lastPath = path;
            }
        }
        this.resource.offer(this.lastPath);
        LOGGER.info(String.format("Leaving critical => %d", this.rounds.getRound()));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .addValue(semaphore)
                      .addValue(resource)
                      .toString();
    }
}
