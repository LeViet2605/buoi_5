package com.example.demo_sample.Common;

import lombok.Getter;

@Getter
public enum CommonErrorCode {

    // --- Account errors ---
    EMAIL_EMPTY("ACC_001", "Email không được để trống"),
    PASSWORD_EMPTY("ACC_002", "Password không được để trống"),
    EMAIL_EXISTS("ACC_003", "Email đã tồn tại"),
    ACCOUNT_NOT_FOUND("ACC_004", "Tài khoản chưa tồn tại"),
    ACCOUNT_LOCKED("ACC_005", "Bạn nhập sai mật khẩu quá 3 lần. Tài khoản tạm thời bị khoá, vui lòng thử lại sau 1 phút"),
    BAD_CREDENTIALS("ACC_006", "Email hoặc mật khẩu không đúng"),
    REFRESH_TOKEN_EMPTY("ACC_007", "Refresh token trống"),
    REFRESH_TOKEN_INVALID("ACC_008", "Refresh token không hợp lệ"),
    TOKEN_EMPTY("ACC_009", "Token không tồn tại"),
    USER_NOT_FOUND("ACC_010", "Không tìm thấy user"),
    UNAUTHORIZED("ACC_011", "Bạn chưa đăng nhập"),
    FORBIDDEN("ACC_012", "Bạn không có quyền thực hiện hành động này"),
    INTERNAL_ERROR("ACC_013", "Lỗi hệ thống"),

    // --- Task errors ---
    TASK_NOT_FOUND("TASK_001", "Task không tồn tại"),
    TASKS_NOT_FOUND("TASK_002", "Không tìm thấy task nào"),

    // --- Success messages ---
    REGISTER_SUCCESS("MSG_001", "Đăng ký thành công"),
    LOGIN_SUCCESS("MSG_002", "Đăng nhập thành công"),
    LOGOUT_SUCCESS("MSG_003", "Đăng xuất thành công"),
    UPDATE_ACCOUNT_SUCCESS("MSG_004", "Cập nhật tài khoản thành công"),
    DELETE_ACCOUNT_SUCCESS("MSG_005", "Xóa tài khoản thành công"),
    CREATE_TASK_SUCCESS("MSG_006", "Tạo task thành công"),
    UPDATE_TASK_SUCCESS("MSG_007", "Update task thành công"),
    DELETE_TASK_SUCCESS("MSG_008", "Xóa Task thành công"),
    ALL_ACCOUNT("MSG_009", "Tất cả các account");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
