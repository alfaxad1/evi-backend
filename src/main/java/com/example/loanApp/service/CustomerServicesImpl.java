package com.example.loanApp.service;

import com.example.loanApp.Mappers.*;
import com.example.loanApp.SecurityConfig.JwtUtils;
import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.*;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.*;
import com.example.loanApp.utility.PhoneNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServicesImpl implements CustomerServices {
    private final CustomerRepository customerRepository;
    private final GurantorRepository guarantorRepository;
    private final RefereeRepository refereeRepository;
    private final GurantorCollateralRepository gurantorCollateralRepository;
    private final CustomerCollateralRepository customerCollateralRepository;
    private final CustomerMapper customerMapper;
    private final CustomerCollateralsMapper customerCollateralsMapper;
    private final RefereesMapper refereesMapper;
    private final GuarantorsMapper guarantorsMapper;
    private final GuarantorsCollateralsMapper guarantorsCollateralsMapper;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository officerRepository;

    @Override
    public Customer getCustomer(Integer id) {
        return customerRepository.findById(id).get();
    }

    public GenericResponse<List<CustomerDetailsDto>> getAllCustomers(
            Integer userId,
            String search,
            Pageable pageable
    ) {

        if (search == null) {
            search = "";
        }

        String role = officerRepository.findRoleById(userId);
        Integer branchId = BranchContext.get();

        Page<Customer> page = customerRepository.searchCustomers(userId, search, role, branchId, pageable);

        Page<CustomerDetailsDto> data = page.map(this::convertToDto);
        List<CustomerDetailsDto> dtos = data.getContent();

        ResponseMetaData meta = ResponseMetaData.builder()
                .page(data.getNumber())
                .totalElements(data.getTotalElements())
                .totalPages(data.getTotalPages())
                .limit(data.getSize())
                .build();

        return GenericResponse.<List<CustomerDetailsDto>>builder()
                .data(dtos)
                .message("Customers fetched successfully")
                .status(ResponseStatusEnum.SUCCESS)
                .metaData(meta)
                .build();
    }

    private CustomerDetailsDto convertToDto(Customer customer) {

        CustomerDetailsDto dto = new CustomerDetailsDto();

        dto.setId(customer.getId());
        dto.setFirstName(customer.getFirstName());
        dto.setMiddleName(customer.getMiddleName());
        dto.setLastName(customer.getLastName());
        dto.setPhone(customer.getPhone());
        dto.setNationalId(customer.getNationalId());
        dto.setNationalIdPhoto(customer.getNationalIdPhoto());
        dto.setPassportPhoto(customer.getPassportPhoto());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setGender(customer.getGender());
        dto.setAddress(customer.getAddress());
        dto.setResidenceDetails(customer.getResidenceDetails());
        dto.setCounty(customer.getCounty());
        dto.setOccupation(customer.getOccupation());
        dto.setBusinessName(customer.getBusinessName());
        dto.setBusinessLocation(customer.getBusinessLocation());
        dto.setMonthlyIncome(customer.getMonthlyIncome());
        dto.setCreditScore(customer.getCreditScore());

        // Collaterals
        dto.setCustomerCollaterals(
                customer.getCustomerCollaterals()
                        .stream()
                        .map(c -> new CustomerCollateralsDto(
                                c.getId(),
                                c.getItemName(),
                                c.getItemCount(),
                                c.getAdditionalDetails()))
                        .toList()
        );

        // Guarantors
        dto.setGuarantors(
                customer.getGuarantors()
                        .stream()
                        .map(g -> new GuarantorDto(
                                g.getId(),
                                g.getName(),
                                g.getNationalId(),
                                g.getPhoneNumber(),
                                g.getRelationship(),
                                g.getBusinessLocation(),
                                g.getResidenceDetails(),
                                g.getPassPhoto(),
                                g.getIdPhoto()))
                        .toList()
        );

        // Guarantor Collaterals
        dto.setGuarantorCollaterals(
                customer.getGuarantors().stream()
                        .flatMap(g -> g.getGuarantorCollaterals().stream())
                        .map(gc -> new GuarantorCollateralDto(
                                gc.getId(),
                                gc.getItemName(),
                                gc.getItemCount(),
                                gc.getAdditionalDetails()
                        ))
                        .toList()
        );

        // Referees
        dto.setReferees(
                customer.getReferees()
                        .stream()
                        .map(r -> new RefereeDto(
                                r.getId(),
                                r.getName(),
                                r.getIdNumber(),
                                r.getPhoneNumber(),
                                r.getRelationship()))
                        .toList()
        );

        return dto;
    }

    @Override
    public void createCustomer(CreateCustomerRequest customerRequest,
                               MultipartFile nationalIdPhoto,
                               MultipartFile passportPhoto,
                               MultipartFile guarantorIdPhoto,
                               MultipartFile guarantorPassPhoto) {
        try {
            String phoneNumber = customerRequest.getCustomerDetails().getPhone();
            String idNumber = customerRequest.getCustomerDetails().getNationalId();

            if (customerRepository.existsByPhoneAndNationalId(phoneNumber, idNumber)) {
                throw new IllegalArgumentException(
                        "Customer with the same phone number or ID number already exists"
                );
            }

            User user = userRepository.findUserById(customerRequest.getCustomerDetails().getUserId());

            Customer customer = customerMapper.toEntity(customerRequest.getCustomerDetails());
            customer.setUser(user);
            customer.setBranch(user.getBranch());

            // Save photos and set file paths
            if (nationalIdPhoto != null && !nationalIdPhoto.isEmpty()) {
                String path = fileStorageService.saveFile(nationalIdPhoto);
                customer.setNationalIdPhoto(path);
            }

            if (passportPhoto != null && !passportPhoto.isEmpty()) {
                String path = fileStorageService.saveFile(passportPhoto);
                customer.setPassportPhoto(path);
            }
            customerRepository.save(customer);

            List<CustomerCollateral> customerCollateral = customerCollateralsMapper.toEntities(customerRequest.getCollaterals());
            for (CustomerCollateral collateral : customerCollateral) {
                collateral.setCustomer(customer);
            }
            customerCollateralRepository.saveAll(customerCollateral);

            List<Referee> referees = refereesMapper.toEntities(customerRequest.getReferees());
            for (Referee referee : referees) {
                referee.setCustomer(customer);
            }
            refereeRepository.saveAll(referees);

            List<Guarantor> guarantors = guarantorsMapper.toEntities(customerRequest.getGuarantors());
            for(Guarantor guarantor : guarantors) {
                guarantor.setCustomer(customer);

                if (guarantorIdPhoto != null && !guarantorIdPhoto.isEmpty()) {
                    String idPath = fileStorageService.saveFile(guarantorIdPhoto);
                    guarantor.setIdPhoto(idPath);
                }

                if (guarantorPassPhoto != null && !guarantorPassPhoto.isEmpty()) {
                    String passPath = fileStorageService.saveFile(guarantorPassPhoto);
                    guarantor.setPassPhoto(passPath);
                }
            }
            guarantorRepository.saveAll(guarantors);

            List<GuarantorCollateral> guarantorCollaterals = guarantorsCollateralsMapper.toEntities(customerRequest.getGuarantorCollaterals());
            for (GuarantorCollateral guarantorCollateral : guarantorCollaterals) {
                guarantors.forEach(guarantorCollateral::setGuarantor);
            }
            gurantorCollateralRepository.saveAll(guarantorCollaterals);

        } catch (Exception e) {
            throw new RuntimeException("Error saving customer: " +e.getMessage());
        }

    }

    @Override
    public void deleteCustomer(Integer id) {
        customerRepository.deleteById(id);
    }

    @Override
    public List<CustomersData> findCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);

        List<CustomersData> dataList = new ArrayList<>();

        for (Customer customer : customers) {
            CustomersData data = CustomersData.builder()
                    .id(customer.getId())
                    .name(customer.getFirstName() + " " + customer.getLastName())
                    .build();
            dataList.add(data);
        }
        return dataList;
    }
}
