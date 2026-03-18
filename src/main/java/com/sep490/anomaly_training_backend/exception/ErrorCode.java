package com.sep490.anomaly_training_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //range 1xxx: Success
    SUCCESS(1000, "Success", HttpStatus.OK),

    //range 2xxx: Client-side errors
    METHOD_NOT_ALLOWED(2001, "HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED),
    INVALID_DATA_TYPE(2024, "Invalid type for parameter: {name}", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_FORMAT(2025, "Invalid request format. Please check your input.", HttpStatus.BAD_REQUEST),

    //range 3xxx: Auth & Security
    UNAUTHORIZED(3001, "Authentication required. Please login.", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(3002, "Invalid username or password.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(3003, "User not found.", HttpStatus.NOT_FOUND),
    INVALID_REFRESH_TOKEN(3004, "Invalid or expired refresh token.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(3005, "Refresh token has expired. Please login again.", HttpStatus.UNAUTHORIZED),
    USER_ACCOUNT_INACTIVE(3006, "User account is inactive.", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSION(3007, "You do not have permission to perform this action.", HttpStatus.FORBIDDEN),
    FI_PERMISSION_REQUIRED(3008, "Access denied: Final Inspection (FI) role required.", HttpStatus.FORBIDDEN),

    //range 4xxx: Server-side errors
    INTERNAL_SERVER_ERROR(4000, "Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ERROR_KEY(4001, "The error key is misspelled or does not exist.", HttpStatus.INTERNAL_SERVER_ERROR),
    LOAD_ADDRESS_JSON_FAIL(4002, "Error loading address JSON file.", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_FAILED(4003, "Transaction failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_SAVE_ERROR(4004, "Error saving data to the database.", HttpStatus.INTERNAL_SERVER_ERROR),
    MINIO_UPLOAD_ERROR(4005, "Error while uploading file to storage.", HttpStatus.INTERNAL_SERVER_ERROR),
    MINIO_DELETE_ERROR(4006, "Error while deleting file from storage.", HttpStatus.INTERNAL_SERVER_ERROR),

    //range 6xxx: General Business logic errors
    USERNAME_ALREADY_EXISTS(6001, "Username already exists.", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(6002, "Email already exists.", HttpStatus.CONFLICT),
    EMPLOYEE_CODE_ALREADY_LINKED(6003, "This employee code is already linked to an account.", HttpStatus.CONFLICT),
    EMPLOYEE_NOT_FOUND(6004, "Employee not found.", HttpStatus.NOT_FOUND),
    INVALID_ROLE_IDS(6005, "One or more role IDs are invalid.", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(6006, "Role not found.", HttpStatus.NOT_FOUND),
    ROLE_CODE_ALREADY_EXISTS(6007, "Role code already exists.", HttpStatus.CONFLICT),
    SYSTEM_ROLE_MODIFICATION_NOT_ALLOWED(6008, "Cannot modify a system role.", HttpStatus.FORBIDDEN),
    SYSTEM_ROLE_DELETION_NOT_ALLOWED(6009, "System roles cannot be deleted.", HttpStatus.FORBIDDEN),
    DEFECT_NOT_FOUND(6010, "Defect not found.", HttpStatus.NOT_FOUND),
    TEAM_NOT_FOUND(6011, "Team not found.", HttpStatus.NOT_FOUND),
    GROUP_NOT_FOUND(6012, "Group not found.", HttpStatus.NOT_FOUND),
    GROUP_NAME_ALREADY_EXISTS(6013, "Group name already exists.", HttpStatus.CONFLICT),
    TEAM_NAME_ALREADY_EXISTS(6014, "Team name already exists.", HttpStatus.CONFLICT),
    PRODUCT_ALREADY_DELETED(6015, "Product is already deleted.", HttpStatus.BAD_REQUEST),
    PROCESS_CODE_ALREADY_EXISTS(6016, "Process code already exists.", HttpStatus.CONFLICT),
    SECTION_NOT_FOUND(6017, "Section not found.", HttpStatus.NOT_FOUND),
    SECTION_NAME_ALREADY_EXISTS(6018, "Section name already exists.", HttpStatus.CONFLICT),
    EMPLOYEE_SKILL_NOT_FOUND(6019, "Employee skill not found.", HttpStatus.NOT_FOUND),
    NOTIFICATION_TEMPLATE_NOT_FOUND(6020, "Notification template not found.", HttpStatus.NOT_FOUND),
    ONLY_AUTHOR_CAN_EDIT(6021, "Only the author can edit this item.", HttpStatus.FORBIDDEN),

    // Attachment Errors (range 61xx)
    INVALID_FILE_TYPE(6100, "Only image files (JPG, PNG, etc.) are allowed.", HttpStatus.BAD_REQUEST),
    NO_FILE_SELECTED(6101, "Please select at least one file to upload.", HttpStatus.BAD_REQUEST),
    MAX_FILES_EXCEEDED(6102, "You can only upload a maximum of {maxFiles} files at a time.", HttpStatus.BAD_REQUEST),
    ATTACHMENT_NOT_FOUND(6103, "Attachment not found.", HttpStatus.NOT_FOUND),

    // Training Plan Errors (range 70xx)
    INVALID_DATE_RANGE(7001, "End date cannot be before start date.", HttpStatus.BAD_REQUEST),
    USER_NOT_TEAM_LEAD(7002, "You are not a team leader of any team.", HttpStatus.FORBIDDEN),
    NO_PERMISSION_FOR_GROUP(7003, "You do not have permission to create a plan for this group.", HttpStatus.FORBIDDEN),
    PRODUCT_LINE_NOT_FOUND(7004, "Product line not found.", HttpStatus.NOT_FOUND),
    PRODUCT_LINE_NOT_IN_GROUP(7005, "The selected product line does not belong to the chosen group.", HttpStatus.BAD_REQUEST),
    TRAINING_PLAN_NOT_FOUND(7006, "Training plan not found.", HttpStatus.NOT_FOUND),
    INVALID_TRAINING_PLAN_STATUS(7007, "This action cannot be performed in the current plan status.", HttpStatus.BAD_REQUEST),
    MISSING_SCHEDULE(7008, "At least one training schedule is required.", HttpStatus.BAD_REQUEST),
    INVALID_DAY_OF_MONTH(7009, "The planned day is not valid for the selected month.", HttpStatus.BAD_REQUEST),
    MISSING_ACTION_IN_DETAIL(7010, "The 'action' field is missing in the detail request.", HttpStatus.BAD_REQUEST),
    MISSING_EMPLOYEE_ID(7011, "Employee ID is required for this action.", HttpStatus.BAD_REQUEST),
    MISSING_BATCH_ID(7012, "Batch ID is required for this action.", HttpStatus.BAD_REQUEST),
    BATCH_NOT_FOUND(7013, "Batch not found with the provided ID.", HttpStatus.NOT_FOUND),
    MISSING_DETAIL_ID(7014, "Detail ID is required for this action.", HttpStatus.BAD_REQUEST),
    TRAINING_PLAN_DETAIL_NOT_FOUND(7015, "Training plan detail not found.", HttpStatus.NOT_FOUND),
    CANNOT_UPDATE_COMPLETED_DETAIL(7016, "Cannot update a completed training detail.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_COMPLETED_DETAIL(7017, "Cannot delete a completed training detail.", HttpStatus.BAD_REQUEST),
    PLAN_HAS_NO_DETAILS(7018, "The plan must have at least one detail before submission.", HttpStatus.BAD_REQUEST),
    MISSING_PLAN_TITLE(7019, "The plan's title cannot be empty.", HttpStatus.BAD_REQUEST),
    MISSING_EMPLOYEE_IN_DETAIL(7020, "A detail line is missing employee information.", HttpStatus.BAD_REQUEST),
    MISSING_PROCESS_IN_DETAIL(7021, "A detail line is missing process information.", HttpStatus.BAD_REQUEST),
    MISSING_PLANNED_DATE_IN_DETAIL(7022, "A detail line is missing the planned date.", HttpStatus.BAD_REQUEST),
    PLANNED_DATE_OUT_OF_RANGE(7023, "A planned date is outside the plan's date range.", HttpStatus.BAD_REQUEST),
    DUPLICATE_TRAINING_SCHEDULE(7024, "Duplicate schedule: The same employee is scheduled for the same date and process.", HttpStatus.CONFLICT),
    PROCESS_NOT_FOUND(7025, "Process not found.", HttpStatus.NOT_FOUND),
    PROCESS_NOT_IN_PRODUCT_LINE(7026, "The selected process does not belong to the plan's product line.", HttpStatus.BAD_REQUEST),
    REVIEW_POLICY_NOT_FOUND(7027, "The selected review policy not found.", HttpStatus.NOT_FOUND),
    REVIEW_REPORT_NOT_FOUND(7028, "The selected review report not found.", HttpStatus.NOT_FOUND),

    // Training Result Errors (range 71xx)
    TRAINING_RESULT_NOT_FOUND(7100, "Training result not found.", HttpStatus.NOT_FOUND),
    TRAINING_RESULT_DETAIL_NOT_FOUND(7101, "Training result detail not found.", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(7102, "Product not found.", HttpStatus.NOT_FOUND),
    TRAINING_SAMPLE_NOT_FOUND(7103, "Training sample not found.", HttpStatus.NOT_FOUND),
    INVALID_TRAINING_RESULT_STATUS(7104, "This action cannot be performed in the current result status.", HttpStatus.BAD_REQUEST),
    MISSING_PROCESS_IN_RESULT_DETAIL(7105, "Cannot submit. Please select a Process for all items.", HttpStatus.BAD_REQUEST),
    TRAINING_SAMPLE_REVIEW_NOT_FOUND(7106, "Training sample review not found.", HttpStatus.NOT_FOUND),

    // Import Errors (range 72xx)
    FILE_IS_EMPTY(7200, "File is empty.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT(7201, "Only .xls or .xlsx files are supported.", HttpStatus.BAD_REQUEST),
    EXCEL_SHEET_NOT_FOUND(7202, "Excel file does not contain any sheet or the first sheet is unreadable.", HttpStatus.BAD_REQUEST),
    PRODUCT_LINE_NOT_IN_HEADER(7203, "ProductLine not found in the file header (Row 1, Column B).", HttpStatus.BAD_REQUEST),
    IMPORT_PARSE_ERROR(7204, "Error parsing rows. Please check the import history for details.", HttpStatus.BAD_REQUEST),
    IMPORT_VALIDATION_ERROR(7205, "Validation failed. Please check the import history for details.", HttpStatus.BAD_REQUEST),
    CANNOT_READ_EXCEL_FILE(7206, "Cannot read the Excel file.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNEXPECTED_IMPORT_ERROR(7207, "An unexpected error occurred during the import process.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMPORT_FAILED(7208, "Import failed. Please check the import history for details.", HttpStatus.BAD_REQUEST),
    INVALID_CELL_VALUE(7209, "Invalid cell value.", HttpStatus.BAD_REQUEST),

    // Proposal Errors (range 73xx)
    DEFECT_PROPOSAL_NOT_FOUND(7300, "Defect proposal not found.", HttpStatus.NOT_FOUND),
    TRAINING_SAMPLE_PROPOSAL_NOT_FOUND(7301, "Training sample proposal not found.", HttpStatus.NOT_FOUND),
    PROPOSAL_HAS_NO_DETAILS(7302, "Cannot submit proposal without details.", HttpStatus.BAD_REQUEST),
    INVALID_DETAIL_ID_FOR_PROPOSAL(7303, "A detail ID does not belong to the proposal.", HttpStatus.BAD_REQUEST),
    MISSING_PROCESS_ID(7304, "Process ID is required.", HttpStatus.BAD_REQUEST),
    MISSING_PROPOSAL_TYPE(7305, "Proposal type is required.", HttpStatus.BAD_REQUEST),
    MISSING_DEFECT_DESCRIPTION(7306, "Defect description is required.", HttpStatus.BAD_REQUEST),
    MISSING_DETECTED_DATE(7307, "Detected date is required.", HttpStatus.BAD_REQUEST),
    MISSING_CATEGORY_NAME(7308, "Category name is required.", HttpStatus.BAD_REQUEST),
    MISSING_TRAINING_DESCRIPTION(7309, "Training description is required.", HttpStatus.BAD_REQUEST),

    // Approval Errors (range 74xx)
    INVALID_ENTITY_STATUS(7400, "Invalid entity status for this operation.", HttpStatus.BAD_REQUEST),
    REJECT_REASON_REQUIRED(7401, "At least one reject reason must be selected.", HttpStatus.BAD_REQUEST),
    INVALID_REJECT_REASON(7402, "One or more reject reasons are invalid.", HttpStatus.BAD_REQUEST),
    INVALID_REQUIRED_ACTION(7403, "The selected required action is invalid.", HttpStatus.BAD_REQUEST),
    APPROVAL_WORKFLOW_NOT_FOUND(7404, "No approval workflow found for this entity type.", HttpStatus.NOT_FOUND),
    APPROVAL_STEP_NOT_FOUND(7405, "No approval step found for the current status.", HttpStatus.NOT_FOUND),
    NOT_DESIGNATED_APPROVER(7406, "You are not the designated approver for this item.", HttpStatus.FORBIDDEN),
    UNSUPPORTED_APPROVER_ROLE(7407, "Unsupported approver role.", HttpStatus.INTERNAL_SERVER_ERROR),

    // Scoring Policy Errors (range 75xx)
    INVALID_POLICY_STATUS(7500, "This action is not allowed for the current policy status.", HttpStatus.BAD_REQUEST),
    INVALID_EFFECTIVE_DATE(7501, "Effective date must be today or in the future.", HttpStatus.BAD_REQUEST),
    INVALID_EXPIRATION_DATE(7502, "Expiration date must be after the effective date.", HttpStatus.BAD_REQUEST),
    MISSING_PRIORITY_TIER(7503, "At least one priority tier is required.", HttpStatus.BAD_REQUEST),
    INVALID_TIER_ORDER(7504, "Tier order must be sequential starting from 1.", HttpStatus.BAD_REQUEST),
    INVALID_METRIC(7505, "An invalid metric was provided for the entity type.", HttpStatus.BAD_REQUEST),
    MISSING_TIER_FILTER(7506, "At least one filter is required for each tier.", HttpStatus.BAD_REQUEST),
    PRIORITY_POLICY_NOT_FOUND(7507, "Priority policy not found.", HttpStatus.NOT_FOUND),
    INVALID_ENTITY_TYPE(7508, "Invalid entity type provided.", HttpStatus.BAD_REQUEST),
    METRIC_NOT_FOUND(7509, "Metric is not found.", HttpStatus.NOT_FOUND),
    INVALID_METRIC_METHOD(7510, "Invalid metric method provided.", HttpStatus.BAD_REQUEST),
    METRIC_CALCULATION_ERROR(7511, "An error occurred while calculating the metric; please check the logs for details.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_METRIC_DEFINITION(7512, "Invalid metric definition provided.", HttpStatus.BAD_REQUEST),
    CLASSIFICATION_NOT_FOUND(7513, "Classification rules not found.", HttpStatus.NOT_FOUND),
    INVALID_CLASSIFICATION_RULE(7514, "Invalid classification rule definition.", HttpStatus.BAD_REQUEST),
    INVALID_CLASSIFICATION_VALUE(7515, "Invalid value for classification.", HttpStatus.BAD_REQUEST),
    POLICY_NOT_FOUND(7516, "Policy not found.", HttpStatus.NOT_FOUND),
    SNAPSHOT_NOT_FOUND(7517, "Snapshot not found.", HttpStatus.NOT_FOUND),
    PRIORITY_SNAPSHOT_NOT_FOUND(7518, "Priority snapshot not found.", HttpStatus.NOT_FOUND),
    FACTORY_CALENDAR_NOT_FOUND(7519, "Factory calendar not found.", HttpStatus.NOT_FOUND),
    METRIC_NOT_IMPLEMENTED(7520, "This metric's compute method is not implemented yet.", HttpStatus.INTERNAL_SERVER_ERROR),

    PROPOSAL_DETAIL_NOT_FOUND(10000, "Proposal detail not found.", HttpStatus.NOT_FOUND),
    ;

    private final int code;
    private final String message;
    private final HttpStatus httpStatusCode;

    ErrorCode(int code, String message, HttpStatus httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}