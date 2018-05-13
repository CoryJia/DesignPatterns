package org.landy.business.validation;

import org.apache.commons.lang3.StringUtils;
import org.landy.business.domain.file.RequestFile;
import org.landy.business.enums.WorkflowEnum;
import org.landy.constants.Constants;
import org.landy.exception.BusinessValidationException;
import org.landy.web.utils.ApplicationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author landyl
 * @create 1:47 PM 05/07/2018
 */
public abstract class AbstractRequestValidation {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRequestValidation.class);

    private static Map<WorkflowEnum,String> requestValidationHandlerMap = new HashMap<>();

    public AbstractRequestValidation() {
        requestValidationHandlerMap.put(this.accessWorkflow(),this.accessBeanName());
    }

    public String validateFileInfo(RequestFile file) {
        if (!file.isFileTypeValid()) {
            return Constants.INVALID_FILE_TYPE + " Carrier Sync Process only accepts text files.";
        }
        String result = validateFileName(file.getFileName());
        if (!isValid(result)) {
            return result;
        }

        return Constants.VALID;
    }

    public String validateSummary(RequestFile file) {
        if (!file.isFileRowCountValid()) {
            return Constants.INVALID_FILE_DETAIL + " File row count does not match the detail row count.";
        }

        if (!file.isFileLayoutValid()) {
            return Constants.INVALID_FILE_DETAIL + " Unknown template layout.";
        }

        if (file.getProcessDate() == null) {
            return Constants.INVALID_FILE_DETAIL + " Process Date is invalid. The format should be \"MM/DD/YYYY\" or \"M/D/YYYY\".";
        }

        return Constants.VALID;
    }

    public String validateHeaders(RequestFile file) {
        if (!file.isHeaderValid()) {
            return Constants.INVALID_FILE_DETAIL + " Column headers do not match the expected template layout.";
        }

        return Constants.VALID;
    }

    public String validateDetails(RequestFile file) {
        StringBuilder errMsg = new StringBuilder(Constants.INVALID_FILE_DETAIL);

        validateFileDetails(errMsg, file);

        if (errMsg.length() > Constants.INVALID_FILE_DETAIL.length()) return errMsg.toString();

        return Constants.VALID;
    }

    /**
     * Generate a AbstractRequestValidation Object
     * @param workflowId
     * @return
     */
    public static final AbstractRequestValidation accessRequestValidationHandler(WorkflowEnum workflowId) {
        String beanName = requestValidationHandlerMap.get(workflowId);
        if(StringUtils.isEmpty(beanName)) {
            LOGGER.error("can not find {}'s component",beanName);
            throw new BusinessValidationException("can not find "+beanName + "'s component,current UPDATE_WORKFLOW_ID is :" + workflowId.getValue());
        }
        return ApplicationUtil.getApplicationContext().getBean(beanName,AbstractRequestValidation.class);
    }


    protected final String generateFileNameResult() {
        return Constants.INVALID_FILE_NAME + " The file name format should be \"" + accessFileNameFormat() + "\"";
    }

    ////////////////////////////////////////////////////////////////////
    //The following methods are implemented by subclasses
    ///////////////////////////////////////////////////////////////////

    /**
     * validate the file details
     * @param errMsg
     * @param requestFile
     * @return
     */
    protected abstract StringBuilder validateFileDetails(StringBuilder errMsg,RequestFile requestFile);

    /**
     * validate the file name
     * @param fileName
     * @return
     */
    protected abstract String validateFileName(String fileName);

    /**
     * return the current CSYNC_UPDATE_WORKFLOW.UPDATE_WORKFLOW_ID
     * @return
     */
    protected abstract WorkflowEnum accessWorkflow();

    /**
     * return the current file name's format ,such as: csync_policy_yyyyMMdd_HHmmss_count.txt
     * @return
     */
    protected abstract String accessFileNameFormat();

    /**
     * return the subclass's spring bean name
     * @return
     */
    protected abstract String accessBeanName();

    private boolean isValid(String result) {
        return Constants.VALID.equals(result);
    }

}
