package edu.asu.diging.gilesecosystem.web.core;

import edu.asu.diging.gilesecosystem.requests.RequestStatus;

/**
 * @deprecated
 *      Use {@link edu.asu.diging.gilesecosystem.web.domain.ITask} instead. This
 *      interface is only kept for migration purposes.
 * @author jdamerow
 *
 */
@Deprecated
public interface ITask {

    public abstract String getTaskHandlerId();

    public abstract void setTaskHandlerId(String taskHandlerId);

    public abstract RequestStatus getStatus();

    public abstract void setStatus(RequestStatus status);

    public abstract void setFileId(String fileId);

    public abstract String getFileId();

}