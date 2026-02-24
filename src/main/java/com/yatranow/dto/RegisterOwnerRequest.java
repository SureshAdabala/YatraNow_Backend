package com.yatranow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOwnerRequest {

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @NotBlank(message = "Agency name is required")
    @Size(min = 2, max = 150, message = "Agency name must be between 2 and 150 characters")
    private String agencyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobile;

    // Compatibility for "mobileNumber"
    public void setMobileNumber(String mobileNumber) {
        this.mobile = mobileNumber;
    }

    // Compatibility for "phone" (sent by frontend)
    public void setPhone(String phone) {
        this.mobile = phone;
    }

    private MultipartFile agencyImage;

    public MultipartFile getAgencyImage() {
        return agencyImage;
    }

    public void setAgencyImage(MultipartFile agencyImage) {
        this.agencyImage = agencyImage;
    }

    // Compatibility for "image" (sent by frontend)
    public void setImage(MultipartFile image) {
        this.agencyImage = image;
    }
}
