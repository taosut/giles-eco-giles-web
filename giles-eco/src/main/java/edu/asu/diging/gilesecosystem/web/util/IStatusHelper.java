package edu.asu.diging.gilesecosystem.web.util;

import edu.asu.diging.gilesecosystem.requests.IRequest;
import edu.asu.diging.gilesecosystem.requests.RequestStatus;
import edu.asu.diging.gilesecosystem.web.core.IDocument;

/**
 * Implementations of this interface provide support for calculating statuses of
 * documents.
 * 
 * @author jdamerow
 *
 */
public interface IStatusHelper {

    /**
     * Method to determine if the processing of a document is done. This method will
     * return true if all requests have either the status {@link RequestStatus.COMPLETE} or
     * the status {@link RequestStatus.FAILED}.
     * 
     * @param document The document for which to determine the processing status.
     * @return true if all requests have either been completed or failed, otherwise false.
     */
    public abstract boolean isProcessingDone(IDocument document);

    /**
     * Method to get the result of a processing phase. This method will look at all 
     * existing request objects of a document, filter them by the provided request class,
     * and then return {@link RequestStatus.FAILED} if at least one of them failed, or 
     * {@link RequestStatus.NEW} if at least one of them is new, or {@link RequestStatus.SUBMITTED}
     * if at least one of them submitted; otherwise it returns {@link RequestStatus.COMPLETE}.
     * 
     * @param requestClass The type of request that should be filtered on.
     * @param document The document for which the processing phase result should be deteremined.
     * @return The request status of the document according to the conditions in the following order:
     * <ol>
     *  <li>{@link RequestStatus.FAILED}: if at least one request failed.</li>
     *  <li>{@link RequestStatus.NEW}: if at least one request is new.</li>
     *  <li>{@link RequestStatus.SUBMITTED}: if at least one request is still being submitted.</li>
     *  <li>{@link RequestStatus.COMPLETE}: in any other case.</li>
     * </ol>
     */
    public abstract RequestStatus getProcessingPhaseResult(Class<? extends IRequest> requestClass, IDocument document);

}