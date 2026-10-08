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
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import com.example.loanApp.enums.Role;
import com.example.loanApp.exceptions.ResourceNotFoundException;

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
    private final FileUploadService uploadService;

    @Override
    public Customer getCustomer(Integer id) {
        return accessibleCustomer(id);
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("Sign in required");
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    private Customer accessibleCustomer(Integer id) {
        User user = currentUser();
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (user.getRole() != Role.admin && (customer.getUser() == null || !Objects.equals(customer.getUser().getId(), user.getId()))) {
            throw new AccessDeniedException("Customer belongs to another officer");
        }
        Integer branchId = BranchContext.get();
        if (branchId != null && (customer.getBranch() == null || !Objects.equals(customer.getBranch().getId(), branchId))) {
            throw new AccessDeniedException("Customer belongs to another branch");
        }
        return customer;
    }

    @Transactional(readOnly = true)
    public CustomerDetailsDto getCustomerDetails(Integer id) {
        return convertToDto(accessibleCustomer(id));
    }

    @Transactional
    public void addGuarantor(Integer customerId, CreateGuarantorRequest request) {
        Customer customer = accessibleCustomer(customerId);
        Guarantor guarantor = new Guarantor();
        guarantor.setCustomer(customer);
        guarantor.setName(request.getName());
        guarantor.setNationalId(request.getNationalId());
        guarantor.setPhoneNumber(request.getPhoneNumber());
        guarantor.setRelationship(request.getRelationship());
        guarantor.setBusinessLocation(request.getBusinessLocation());
        guarantor.setResidenceDetails(request.getResidenceDetails());
        guarantorRepository.save(guarantor);
    }

    @Transactional
    public void addReferee(Integer customerId, CreateRefereeRequest request) {
        Customer customer = accessibleCustomer(customerId);
        Referee referee = new Referee();
        referee.setCustomer(customer);
        referee.setName(request.getName());
        referee.setIdNumber(request.getIdNumber());
        referee.setPhoneNumber(request.getPhoneNumber());
        referee.setRelationship(request.getRelationship());
        refereeRepository.save(referee);
    }

    public GenericResponse<List<CustomerDetailsDto>> getAllCustomers(
            Integer userId,
            String search,
            Pageable pageable
    ) {

        if (search == null) {
            search = "";
        }

        User caller = currentUser();
        if (caller.getRole() != Role.admin) userId = caller.getId();
        String role = caller.getRole().name();
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
                                g.getIdPhoto(),
                                g.getPassPhoto()))
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
    @Transactional
    public Integer createCustomer(CreateCustomerRequest customerRequest,
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

            User caller = currentUser();
            User user = caller.getRole() == Role.admin ? userRepository.findUserById(customerRequest.getCustomerDetails().getUserId()) : caller;

            Customer customer = customerMapper.toEntity(customerRequest.getCustomerDetails());
            customer.setUser(user);
            customer.setBranch(user.getBranch());

            // Save photos and set file paths
            if (nationalIdPhoto != null && !nationalIdPhoto.isEmpty()) {
                String path = uploadService.uploadToHetzner(idNumber, nationalIdPhoto);
                customer.setNationalIdPhoto(path);
            }

            if (passportPhoto != null && !passportPhoto.isEmpty()) {
                String path = uploadService.uploadToHetzner(idNumber, passportPhoto);
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
                    String idPath = uploadService.uploadToHetzner(idNumber, guarantorIdPhoto);
                    guarantor.setIdPhoto(idPath);
                }

                if (guarantorPassPhoto != null && !guarantorPassPhoto.isEmpty()) {
                    String passPath = uploadService.uploadToHetzner(idNumber, guarantorPassPhoto);
                    guarantor.setPassPhoto(passPath);
                }
            }
            guarantorRepository.saveAll(guarantors);

            List<GuarantorCollateral> guarantorCollaterals = guarantorsCollateralsMapper.toEntities(customerRequest.getGuarantorCollaterals());
            for (GuarantorCollateral guarantorCollateral : guarantorCollaterals) {
                guarantors.forEach(guarantorCollateral::setGuarantor);
            }
            gurantorCollateralRepository.saveAll(guarantorCollaterals);
            return customer.getId();

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

    @Override
    public void transferCustomer(TransferCustomerRequest request) {
        try{
            List<Customer> customers = customerRepository.findAllById(request.getCustomerIds());
            User user = userRepository.findUserById(request.getOfficerId());
            for (Customer customer : customers) {
                customer.setUser(user);
                customerRepository.save(customer);
            }
        }catch (Exception e){
            throw new RuntimeException("Error saving customer: " +e.getMessage());
        }
    }
}
