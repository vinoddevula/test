package com.honeywell.coreptdu.datatypes.duplicaterecords.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.request.CustomerQuerySearchDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.request.CustomerRequestDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.entity.Customer;
import com.honeywell.coreptdu.datatypes.duplicaterecords.repository.ICustomerRepository;
import com.honeywell.coreptdu.datatypes.duplicaterecords.service.ICustomerService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * Customer Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class CustomerServiceImpl implements ICustomerService {

	@Autowired
	ICustomerRepository customerRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of Customer with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of Customer based on the specified
	 *         page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Customer>>> getAllCustomer(int page, int rec) {
		BaseResponse<List<Customer>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all Customer Data");
			if (page == -1 && rec == -1) {
				List<Customer> customer = customerRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, customer));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<Customer> customerPages = customerRepository.findAll(pages);
			if (customerPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						customerPages.getContent(), customerPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all Customer data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
		}
	}

	/**
	 * Retrieves a specific Customer data by its ID.
	 *
	 * @param id The ID of the Customer to retrieve.
	 * @return A ResponseDto containing the Customer entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<Customer>> getCustomerById(Long id) {
		BaseResponse<Customer> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching Customer Data By Id");
			Optional<Customer> customer = customerRepository.findById(id);
			if (customer.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, customer.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching Customer data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
		}
	}

	/**
	 * Creates new Customers based on the provided list of DTOs.
	 *
	 * @param createcustomers The list of DTOs containing data for creating
	 *                        Customer.
	 * @return A ResponseDto containing the list of created Customer entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Customer>>> createCustomer(List<CustomerRequestDto> customersCreate) {
		BaseResponse<List<Customer>> responseObj = new BaseResponse<>();
		List<Customer> createdCustomers = new ArrayList<>();

//		for (CustomerRequestDto customerCreate : customersCreate) {
//			try {
//				log.info("Creating Customer Data");
//				Customer customer = new Customer();
//				Customer createdCustomer = customerRepository.save(customer);
//				createdCustomers.add(createdCustomer);
//			} catch (Exception ex) {
//				log.error("An error occurred while creating Customer data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
//			}
//		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdCustomers));
	}

	/**
	 * Updates existing Customers based on the provided list of DTOs.
	 *
	 * @param customersUpdate The list of DTOs containing data for updating
	 *                        Customer.
	 * @return A ResponseDto containing the list of updated Customer entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Customer>>> updateCustomer(List<CustomerRequestDto> customersUpdate) {
		BaseResponse<List<Customer>> responseObj = new BaseResponse<>();
		List<Customer> updatedCustomers = new ArrayList<>();

//		for (CustomerRequestDto customerUpdate : customersUpdate) {
//			try {
//				log.info("Updating Customer Data");
////				if (existingCustomerOptional.isPresent()) {
////					Customer existingCustomer = existingCustomerOptional.get();
////					Customer updatedCustomer = customerRepository.save(existingCustomer);
////					updatedCustomers.add(updatedCustomer);
////				} else {
//					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
////				}
//			} catch (Exception ex) {
//				log.error("An error occurred while updating Customer data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
//			}
//		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedCustomers));
	}

	/**
	 * Deletes existing Customers based on the provided list of DTOs.
	 *
	 * @param deletecustomers The list of DTOs containing data for deleting
	 *                        Customer.
	 * @return A ResponseDto containing the list of deleted Customer entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Customer>>> deleteCustomer(List<CustomerRequestDto> customerDeletes) {
		BaseResponse<List<Customer>> responseObj = new BaseResponse<>();
		List<Customer> deletedCustomers = new ArrayList<>();

		for (CustomerRequestDto customerDelete : customerDeletes) {
			try {
				log.info("Deleting Customer Data");
				Optional<Customer> existingCustomerOptional = customerRepository
						.findById(customerDelete.getCustomerIdent());
				if (existingCustomerOptional.isPresent()) {
					Customer existingCustomer = existingCustomerOptional.get();
					customerRepository.deleteById(existingCustomer.getCustomerIdent());
					deletedCustomers.add(existingCustomer);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while deleting Customer data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedCustomers));
	}

	@Override
	public ResponseEntity<ResponseDto<List<Customer>>> searchCustomer(CustomerQuerySearchDto customerQuerySearch,
			int page, int rec) {
		BaseResponse<List<Customer>> responseObj = new BaseResponse<>();
		List<Customer> searchCustomers = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(customerQuerySearch, "customer", "", "customer_ident", true,
					page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(customerQuerySearch, "customer", "", "customer_ident", false,
					page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				records = app.executeQuery(searchQuery, page, rec);
			}

			for (Record searchRec : records) {
				searchCustomers.add(app.mapResultSetToClass(searchRec, Customer.class));
			}
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchCustomers, total));
		} catch (Exception ex) {
			log.error("An error occurred while querying the data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
		}
	}
}
-------------------
package com.honeywell.coreptdu.datatypes.duplicaterecords.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.request.DcrListRequestDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.response.CustListResponseDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.response.DcrListResponseDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.response.ProcessingCycleResponseDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.service.IDuplicateRecordsLovService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DuplicateRecordsLovServiceImpl implements IDuplicateRecordsLovService {
	@Autowired
	private IApplication app;

	@Override
	public ResponseEntity<ResponseDto<List<CustListResponseDto>>> custList(Integer page, Integer rec) {
		BaseResponse<List<CustListResponseDto>> responseObj = new BaseResponse<>();
		List<CustListResponseDto> resList = new ArrayList<>();
		try {
			String lovQuery = """
								SELECT CUSTOMER_IDENT, CUSTOMER_NAME
					FROM CUSTOMER
					order by 1
								""";
			List<Record> lovRecs = app.executeQuery(lovQuery);
			for (Record lovRec : lovRecs) {
				CustListResponseDto custlistDto = new CustListResponseDto();
				custlistDto.setCustomerIdent(lovRec.getString());
				custlistDto.setCustomerName(lovRec.getString());
				resList.add(custlistDto);
			}
			log.info("custList Lov Executed Successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resList));
		} catch (Exception e) {
			log.error("Error while Executing custList Lov", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<ProcessingCycleResponseDto>>> processingCycle(Integer page, Integer rec) {
		BaseResponse<List<ProcessingCycleResponseDto>> responseObj = new BaseResponse<>();
		List<ProcessingCycleResponseDto> resList = new ArrayList<>();
		try {
			String lovQuery = """
								SELECT cycle
					FROM cycle
					order by cycle desc
								""";
			List<Record> lovRecs = app.executeQuery(lovQuery);
			for (Record lovRec : lovRecs) {
				ProcessingCycleResponseDto processingcycleDto = new ProcessingCycleResponseDto();
				processingcycleDto.setCycle(lovRec.getString());
				resList.add(processingcycleDto);
			}
			log.info("processingCycle Lov Executed Successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resList));
		} catch (Exception e) {
			log.error("Error while Executing processingCycle Lov", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<DcrListResponseDto>>> dcrList(Integer page, Integer rec,
			DcrListRequestDto reqDto) {
		BaseResponse<List<DcrListResponseDto>> responseObj = new BaseResponse<>();
		List<DcrListResponseDto> resList = new ArrayList<>();
		try {
			String lovQuery = """
								SELECT DCR_Number,Reason_for_change FROM Search_by_Navdb_Assignee
					WHERE NavDB_ID = ?
					AND Effectivity_cycle = ?
					AND DCR_overall_status = 'OPEN'
					GROUP BY DCR_Number,Reason_for_change
					ORDER BY 1 DESC
								""";
			List<Record> lovRecs = app.executeQuery(lovQuery, reqDto.getCustListBlkCustomer(),
					reqDto.getParameterProcessingCycle());
			for (Record lovRec : lovRecs) {
				DcrListResponseDto dcrlistDto = new DcrListResponseDto();
				dcrlistDto.setDcrNumber(lovRec.getString());
				dcrlistDto.setReasonForChange(lovRec.getString());
				resList.add(dcrlistDto);
			}
			log.info("dcrList Lov Executed Successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resList));
		} catch (Exception e) {
			log.error("Error while Executing dcrList Lov", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}
----------------------------------
package com.honeywell.coreptdu.datatypes.duplicaterecords.serviceimpl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.coreptmenummb.serviceimpl.CoreptMenuMmbServiceImpl;
import com.honeywell.coreptdu.datatypes.duplicaterecords.block.CopyDetailsDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.block.CustListBlk;
import com.honeywell.coreptdu.datatypes.duplicaterecords.block.Dupl;
import com.honeywell.coreptdu.datatypes.duplicaterecords.block.DuplRecordsDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.block.Message;
import com.honeywell.coreptdu.datatypes.duplicaterecords.block.Webutil;
import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.request.DuplicateRecordsTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.dto.response.DuplicateRecordsTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.duplicaterecords.entity.Customer;
import com.honeywell.coreptdu.datatypes.duplicaterecords.service.IDuplicateRecordsTriggerService;
import com.honeywell.coreptdu.datatypes.exportdestination.block.SelectOptions;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.request.ExportDestinationTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.response.ExportDestinationTriggerResponseDto;
import com.honeywell.coreptdu.exception.AlertException;
import com.honeywell.coreptdu.exception.EDuplicate;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.exception.FormTriggerFailureException;
import com.honeywell.coreptdu.exception.NoDataFoundException;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.Global;
import com.honeywell.coreptdu.global.dto.Globals;
import com.honeywell.coreptdu.global.dto.Parameter;
import com.honeywell.coreptdu.global.dto.ReportDetail;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.global.dto.SystemDto;
import com.honeywell.coreptdu.global.forms.AlertDetail;
import com.honeywell.coreptdu.global.forms.BlockDetail;
import com.honeywell.coreptdu.global.forms.Event;
import com.honeywell.coreptdu.global.forms.WindowDetail;
import com.honeywell.coreptdu.pkg.body.RefreshMasterLibrary;
import com.honeywell.coreptdu.pkg.spec.IDisplayAlert;
import com.honeywell.coreptdu.pll.CoreptLib;
import com.honeywell.coreptdu.pll.PitssCon30;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.GenericNativeQueryHelper;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureOutParameter;
import com.honeywell.coreptdu.utils.oracleutils.Block;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.HoneyWellUtils;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.PropertyHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroup;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroupColumn;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;

@Slf4j
@Service
@RequestScope
public class DuplicateRecordsTriggerServiceImpl extends GenericTemplateForm<DuplicateRecordsTriggerServiceImpl>
		implements IDuplicateRecordsTriggerService {

	@Getter
	@Setter
	private Webutil webutil = new Webutil();
	@Getter
	@Setter
	private Block<Message> message = new Block<>();
	@Getter
	@Setter
	private Block<CustListBlk> custListBlk = new Block<>();
	@Getter
	@Setter
	private Block<Customer> customer = new Block<>();
	@Getter
	@Setter
	private Dupl dupl = new Dupl();
	@Getter
	@Setter
	private Global global = new Global();
	@Getter
	@Setter
	private Globals globals = new Globals();
	@Getter
	@Setter
	private SystemDto system = new SystemDto();
	@Getter
	@Setter
	private Parameter parameter = new Parameter();
	@Getter
	@Setter
	private List<Event> event = new ArrayList<>();
	@Getter
	@Setter
	private HashMap<String, RecordGroup> groups = new HashMap<>();
	@Autowired
	private IApplication app;
	@Autowired
	private CoreptLib coreptLib;
	@Autowired
	private PitssCon30 pitssCon30;
	@Autowired
	private RefreshMasterLibrary refreshMasterLibrary;
	@Autowired
	private IDisplayAlert displayAlert;
	@Autowired
	private CoreptMenuMmbServiceImpl coreptMenuMmbServiceImpl;
	@Getter
	@Setter
	private AlertDetail alertDetails = new AlertDetail();

	@Getter
	@Setter
	private Map<String, WindowDetail> windows = new HashMap<>();

	@Getter
	@Setter
	private SelectOptions selectOptions = new SelectOptions();

	private List<String> msglist = new ArrayList<>();
	@Autowired
	private GenericNativeQueryHelper genericNativeQueryHelper;

	@Override
	public void modificationHistory() throws Exception {
		log.info("modificationHistory Executing");
//		String query = "";
//		Record rec = null;
		try {
			log.info("modificationHistory Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing modificationHistory" + e.getMessage());
			throw e;

		}
	}

	@Override
    public void checkParameters() throws Exception {
      log.info("checkParameters Executing");
//		String query = "";
//		Record rec = null;
      try {
        Integer vNcheck = 0;
//			String vLastQuery = null;
        String fname = HoneyWellUtils.toCamelCase(system.getCurrentForm());
        global.setRemoveCostIndex(dupl.getBlnkCostIndx());
        if (Objects.equals(dupl.getNrecordsT(), "ALL")) {
          if (Objects.equals(dupl.getGeninhouse(), "Y")) {
            alertDetails.getCurrent();
            if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
              displayAlert.moreButtonsColour("gnote", "All Records?",
                  "The system will duplicate generated in house records of customer '"
                      + global.getCurrCustomerIdent() + "' in cycle " + global.getRecordCycle()
                      + " under the selected customer(s) for cycle " + dupl.getNewCycle(),
                  "Continue", "Cancel", "");
              OracleHelpers.bulkClassMapper(displayAlert, this);
              alertDetails.createNewRecord("allRecords1");
              throw new AlertException(event, alertDetails);
            } else {
              vNcheck = alertDetails.getAlertValue("allRecords1", alertDetails.getCurrentAlert());
            }
          } else {
            alertDetails.getCurrent();
            if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
              displayAlert.moreButtonsColour("gnote", "All Records?",
                  "The system will duplicate all '" + global.getCurrCustomerIdent()
                      + "' records in cycle " + global.getRecordCycle()
                      + " under the selected customer(s) for cycle " + dupl.getNewCycle(),
                  "Continue", "Cancel", "");
              OracleHelpers.bulkClassMapper(displayAlert, this);
              alertDetails.createNewRecord("allRecords1");
              throw new AlertException(event, alertDetails);
            } else {
              vNcheck = alertDetails.getAlertValue("allRecords1", alertDetails.getCurrentAlert());
            }
          }
          if (Objects.equals(vNcheck, 1)) {
            global.setNewCustomerIdent(dupl.getNewReplaceCust());
            global.setNewCycle(dupl.getNewCycle().toString());
            global.setNumToDuplicate("ALL");
            global.setGeninhouse(dupl.getGeninhouse());
            global.setDoDuplicate("Y");

            goBlock("message", "");
            setItemProperty("closeIt", ENABLED, PROPERTY_FALSE);
            setItemProperty("saveIt", ENABLED, PROPERTY_FALSE);

            if (Objects.equals(dupl.getGeninouthouse(), "Y")) {
              global.setGeninouthouse(dupl.getGeninouthouse());
            }

            else if (Objects.equals(global.getRecordType(), "S")) {
              global.setGeninouthouse("Y");
            }

            else {
              global.setGeninouthouse("S");
            }

            List<String> result = coreptLib.duplicatestep2(this);

            for (int i = 0; i <= result.size() - 1;) {
              message.getRow(i).setRuntimeMessage(result.get(i));
              message.add(new Message());
              i++;
            }
            for (int j = 0; j <= message.size() - 1; j++) {
              if (Objects.equals(message.getRow(j).getRuntimeMessage(), null)) {
                message.remove(j);
              }
            }

            hideView("custListCan");
            setItemProperty("closeIt", ENABLED, PROPERTY_TRUE);
            setItemProperty("saveIt", ENABLED, PROPERTY_TRUE);
          }

          else {
            throw new FormTriggerFailureException(event);
          }
        }

        else if (Objects.equals(dupl.getNrecordsS(), "SET")
            || Objects.equals(dupl.getNrecordsT(), "SET")) {
          if (Objects.equals(dupl.getNrecordsS(), "SET")
              && Objects.equals(dupl.getDatatyp(), "STD")) {
            alertDetails.getCurrent();
            if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
              displayAlert.moreButtonsColour("gnote", "Set of Records?",
                  "The system will duplicate displayed records which still\nmatch the last query in cycle "
                      + global.getRecordCycle() + " for\ncycle " + dupl.getNewCycle(),
                  "Continue", "Cancel", "");
              OracleHelpers.bulkClassMapper(displayAlert, this);
              alertDetails.createNewRecord("setRecords1");
              throw new AlertException(event, alertDetails);
            } else {
              vNcheck = alertDetails.getAlertValue("setRecords1", alertDetails.getCurrentAlert());
            }
          } else {
            alertDetails.getCurrent();
            if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
              displayAlert.moreButtonsColour("gnote", "Set of Records?",
                  "The system will duplicate displayed records which still\nmatch the last query in cycle "
                      + global.getRecordCycle() + " under the\nselected customer(s) for cycle "
                      + dupl.getNewCycle(),
                  "Continue", "Cancel", "");
              OracleHelpers.bulkClassMapper(displayAlert, this);
              alertDetails.createNewRecord("setRecords1");
              throw new AlertException(event, alertDetails);
            } else {
              vNcheck = alertDetails.getAlertValue("setRecords1", alertDetails.getCurrentAlert());
            }
          }

          if (Objects.equals(vNcheck, 1)) {
            global.setNewCustomerIdent(dupl.getNewReplaceCust());
            global.setNewCycle(toString(dupl.getNewCycle()));
            global.setSecondIdent(rtrim(dupl.getIdent2nd()));
            global.setNumToDuplicate("SET");
            global.setGeninhouse(dupl.getGeninhouse());
            global.setDoDuplicate("Y");
            goBlock("message", "");
            setItemProperty("closeIt", ENABLED, PROPERTY_FALSE);
            setItemProperty("saveIt", ENABLED, PROPERTY_FALSE);

            if ("Y".equals(dupl.getGeninouthouse())) {
              global.setGeninouthouse(dupl.getGeninouthouse());
            } else if ("S".equals(global.getRecordType())) {
              global.setGeninouthouse("Y");
            } else {
              global.setGeninouthouse("S");
            }
            List<String> result = coreptLib.duplicatestep2(this);
            for (int i = 0; i <= result.size() - 1;) {
              message.getRow(i).setRuntimeMessage(result.get(i));
              message.add(new Message());
              i++;
            }
            for (int j = 0; j <= message.size() - 1; j++) {
              if (Objects.equals(message.getRow(j).getRuntimeMessage(), null)) {
                message.remove(j);
              }
            }

            if (Objects.equals(dupl.getDatatyp(), "STD") && Objects.equals(dupl.getDupRep(), "D")
                && OracleHelpers.isNullorEmpty(dupl.getNewReplaceCust())) {
              Integer lnRecCount = app.executeFunction(BigDecimal.class, "CPTS",
                  "dup_count_records_fun", "forms_utilities", OracleTypes.NUMBER,
                  new ProcedureInParameter("p_table", global.getCurrentTable(),
                      OracleTypes.VARCHAR),
                  new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR))
                  .intValue();

              for (int j = 0; j < message.size(); j++) {
                if (message.getRow(j).getRuntimeMessage().contains("Record Already Exists")
                    || message.getRow(j).getRuntimeMessage()
                        .contains(lnRecCount + " standard record is")
                    || message.getRow(j).getRuntimeMessage().contains(" standard record is")
                    || message.getRow(j).getRuntimeMessage().contains("0 standard record")) {
                  message.remove(j);
                }
              }

            }

            hideView("custListCan");
            setItemProperty("closeIt", ENABLED, PROPERTY_TRUE);
            setItemProperty("saveIt", ENABLED, PROPERTY_TRUE);
          } else {
            throw new FormTriggerFailureException(event);
          }
        } else {
          global.setNewGateIdent(dupl.getNewGateIdent());

          if ("T".equals(global.getRecordType())) {
            if (OracleHelpers.isNullorEmpty(dupl.getIdent1st())
                && OracleHelpers.isNullorEmpty(dupl.getIdent2nd())) {
              alertDetails.getCurrent();
              if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                displayAlert.moreButtonsColour("gnote", "Current Record",
                    "The system will duplicate the current '" + global.getCurrCustomerIdent()
                        + "' customer's\nrecord under the selected Customer Ident(s) for cycle\n"
                        + dupl.getNewCycle(),
                    "Continue", "Cancel", "");
                OracleHelpers.bulkClassMapper(displayAlert, this);
                alertDetails.createNewRecord("currentRecord1");
                throw new AlertException(event, alertDetails);
              } else {
                vNcheck = alertDetails.getAlertValue("currentRecord1",
                    alertDetails.getCurrentAlert());
              }
            } else if (!OracleHelpers.isNullorEmpty(dupl.getIdent1st())) {
              alertDetails.getCurrent();
              if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                displayAlert.moreButtonsColour("gnote", "Current Record",
                    "The system will duplicate the current '" + global.getCurrCustomerIdent()
                        + "' customer's\nrecord under the selected Customer Ident(s) for cycle\n"
                        + dupl.getNewCycle() + "with new ident '" + dupl.getIdent1st() + "'",
                    "Continue", "Cancel", "");
                OracleHelpers.bulkClassMapper(displayAlert, this);
                alertDetails.createNewRecord("currentRecord1");
                throw new AlertException(event, alertDetails);
              } else {
                vNcheck = alertDetails.getAlertValue("currentRecord1",
                    alertDetails.getCurrentAlert());
              }
            } else if (!OracleHelpers.isNullorEmpty(dupl.getIdent2nd())) {
              alertDetails.getCurrent();
              if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                displayAlert.moreButtonsColour("gnote", "Current Record",
                    "The system will duplicate the current '" + global.getCurrCustomerIdent()
                        + "' customer's\nrecord under the selected Customer Ident(s) for cycle '\n"
                        + dupl.getNewCycle() + "'with new airport/fix ident/cost index '"
                        + dupl.getIdent2nd() + "'",
                    "Continue", "Cancel", null);
                OracleHelpers.bulkClassMapper(displayAlert, this);
                alertDetails.createNewRecord("currentRecord1");
                throw new AlertException(event, alertDetails);
              } else {
                vNcheck = alertDetails.getAlertValue("currentRecord1",
                    alertDetails.getCurrentAlert());
              }
            }
          } else {
            if (!Objects.equals(dupl.getDatatyp(), "STD")) {
              if (OracleHelpers.isNullorEmpty(rtrim(dupl.getIdent1st()))) {
                alertDetails.getCurrent();
                if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                  displayAlert.moreButtonsColour("gnote", "Current Record",
                      "The system will duplicate the current standard record\nunder the selected Customer Ident(s) for cycle "
                          + dupl.getNewCycle(),
                      "Continue", "Cancel", "");
                  OracleHelpers.bulkClassMapper(displayAlert, this);
                  alertDetails.createNewRecord("currentRecord1");
                  throw new AlertException(event, alertDetails);
                } else {
                  vNcheck = alertDetails.getAlertValue("currentRecord1",
                      alertDetails.getCurrentAlert());
                }
              } else {
                alertDetails.getCurrent();
                if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                  displayAlert.moreButtonsColour("gnote", "Current Record",
                      "The system will duplicate the current standard record\nunder the selected Customer Ident(s) for cycle "
                          + dupl.getNewCycle() + "with new ident '" + dupl.getIdent1st() + "'",
                      "Continue", "Cancel", "");
                  OracleHelpers.bulkClassMapper(displayAlert, this);
                  alertDetails.createNewRecord("currentRecord1");
                  throw new AlertException(event, alertDetails);
                } else {
                  vNcheck = alertDetails.getAlertValue("currentRecord1",
                      alertDetails.getCurrentAlert());
                }
              }
            } else {
              if (Objects.equals(rtrim(dupl.getIdent1st()), null)
                  || Objects.equals(rtrim(dupl.getIdent1st()), "")) {
                alertDetails.getCurrent();
                if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                  displayAlert.moreButtonsColour("gnote", "Current Record",
                      "The system will duplicate the current standard record\nto cycle "
                          + dupl.getNewCycle(),
                      "Continue", "Cancel", "");
                  OracleHelpers.bulkClassMapper(displayAlert, this);
                  alertDetails.createNewRecord("currentRecord1");
                  throw new AlertException(event, alertDetails);
                } else {
                  vNcheck = alertDetails.getAlertValue("currentRecord1",
                      alertDetails.getCurrentAlert());
                }
              } else {
                alertDetails.getCurrent();
                if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                  displayAlert.moreButtonsColour("gnote", "Current Record",
                      "The system will duplicate the current standard record\nto cycle "
                          + dupl.getNewCycle() + "with new ident '" + dupl.getIdent1st() + "'",
                      "Continue", "Cancel", "");
                  OracleHelpers.bulkClassMapper(displayAlert, this);
                  alertDetails.createNewRecord("currentRecord1");
                  throw new AlertException(event, alertDetails);
                } else {
                  vNcheck = alertDetails.getAlertValue("currentRecord1",
                      alertDetails.getCurrentAlert());
                }
              }
            }
          }
          if (vNcheck == 1) {
            if (Objects.equals(dupl.getDatatyp(), "STD")) {
              global.setNewCustomerIdent(null);
            } else {
              global.setNewCustomerIdent(dupl.getNewReplaceCust());
            }

            global.setNewCycle(toString(dupl.getNewCycle()));
            global.setNumToDuplicate("ONE");
            global.setDoDuplicate("Y");
            global.setFirstIdent(rtrim(dupl.getIdent1st()));
            global.setSecondIdent(rtrim(dupl.getIdent2nd()));
            global.setSecondIcao(rtrim(dupl.getIcao2nd()));
            goBlock("message", "runtimeMessage");
            setItemProperty("closeIt", ENABLED, PROPERTY_FALSE);
            setItemProperty("saveIt", ENABLED, PROPERTY_FALSE);

            if (Objects.equals(dupl.getGeninouthouse(), "Y")) {
              global.setGeninouthouse(dupl.getGeninouthouse());
            } else if (Objects.equals(global.getRecordType(), "S")) {
              global.setGeninouthouse("Y");
            } else {
              global.setGeninouthouse("S");
            }

            if (Objects.equals(fname, "gates")) {
              duplicateStep2();
            } else {
              // duplicateStep2();
              List<String> result = coreptLib.duplicatestep2(this);
              for (int i = 0; i <= result.size() - 1;) {
                message.getRow(i).setRuntimeMessage(result.get(i));
                message.add(new Message());
                i++;
              }
              for (int j = 0; j <= message.size() - 1; j++) {
                if (Objects.equals(message.getRow(j).getRuntimeMessage(), null)) {
                  message.remove(j);
                }
              }

              if (Objects.equals(dupl.getDatatyp(), "STD") && Objects.equals(dupl.getDupRep(), "D")
                  && OracleHelpers.isNullorEmpty(dupl.getNewReplaceCust())) {

                Integer lnRecCount = app.executeFunction(BigDecimal.class, "CPTS",
                    "dup_count_records_fun", "forms_utilities", OracleTypes.NUMBER,
                    new ProcedureInParameter("p_table", global.getCurrentTable(),
                        OracleTypes.VARCHAR),
                    new ProcedureInParameter("p_where", global.getGLastQuery(),
                        OracleTypes.VARCHAR))
                    .intValue();

                for (int j = 0; j < message.size(); j++) {
                  if (message.getRow(j).getRuntimeMessage().contains("Record Already Exists")
                      || message.getRow(j).getRuntimeMessage()
                          .contains(lnRecCount + " standard record is")
                      || message.getRow(j).getRuntimeMessage().contains(" standard record is")
                      || message.getRow(j).getRuntimeMessage().contains("0 standard record")) {
                    message.remove(j);
                  }
                }

              }
            }

            hideView("custListCan");
            setItemProperty("closeIt", ENABLED, PROPERTY_TRUE);
            setItemProperty("saveIt", ENABLED, PROPERTY_TRUE);
          } else {
            clearMessage();
            throw new FormTriggerFailureException(event);
          }
        }

        log.info("checkParameters Executed Successfully");
      } catch (Exception e) {
        log.error("Error while executing checkParameters" + e.getMessage());
        throw e;

      }
    }

	@Override
	public void deleteOneCust() throws Exception {
		log.info("deleteOneCust Executing");
//		String query = "";
//		Record rec = null;
		try {
			Integer vLength = 0;
//			String vCust1 = null;
//			String vCust2 = null;

			vLength = instr("," + dupl.getNewCustomerIdent() + ",",
					"," + customer.getRow(system.getCursorRecordIndex()).getCustomerIdent() + ",");

			// coverity-fixes
//			if (Objects.equals(vLength, 1)) {
////				vCust1 = null;
//
//			}

//			else {
////				vCust1 = rtrim(ltrim(dupl.getNewCustomerIdent().substring(0, vLength - 2), ", "), ", ");
//
//			}
//			vCust2 = rtrim(ltrim(dupl.getNewCustomerIdent().substring(0), ", "), ", ");
//			dupl.setNewCustomerIdent(ltrim(vCust1 + "," + vCust2, ", "));

			log.info("deleteOneCust Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing deleteOneCust" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void validateCustomer() throws Exception {
		log.info("validateCustomer Executing");
		String query = "";
		Record rec = null;
		try {
			RecordGroup groupId = findGroup("newCustomer");
			Integer vRow = -1;
			Integer vLength = 0;
			String vAllCust = ltrim(rtrim(dupl.getNewCustomerIdent(), ", "));
			String vCust = null;
			Integer vCount = 0;
			String vsSupplier = null;
//			Integer vnCheck = 0;
			String vsCustomer = null;
//			String vsDataSupplName = null;

			vAllCust = null;
			vAllCust = parameter.getRepCust();
			vLength = vAllCust != null ? instr(vAllCust, " ") : 0;
			while (!Objects.equals(vLength, 0)) {
				// Coverity-fixes
				vAllCust = rtrim(vAllCust.substring(0, vLength - 1), ", ") + "," + ltrim(substr(vAllCust, 0));
				vLength = instr(vAllCust, " ");
			}

			dupl.setNewReplaceCust(vAllCust);
			deleteGroupRow("newCustomer", "ALL_ROWS");
			while (!Objects.equals(vAllCust, null)) {
				vLength = instr(vAllCust, ",");
				if (Objects.equals(vLength, 0)) {
					vCust = vAllCust;
					vAllCust = null;
				}

				else {
					vCust = substr(vAllCust, 1, vLength - 1);
					vAllCust = substr(vAllCust, vLength + 1);
				}
				if (!vCust.equals("null")) {
					if (length(vCust) > 3) {
						coreptLib.dspMsg("Invalid customer Ident '" + vCust + "', correct it please.");
						throw new FormTriggerFailureException(event);
					}

					else {

						query = """
								SELECT COUNT (*)
								           from navdb
								          WHERE UPPER (navdb_id) = UPPER (?) OR UPPER (navdb_id) = UPPER (RPAD (?, 3, ' '))
								""";
						rec = app.selectInto(query, vCust, vCust);
						vCount = rec.getInt();
						if (Objects.equals(vCount, 0)) {
							coreptLib.dspMsg("Customer Ident '" + vCust
									+ "' not present in database. Please correct/delete it.");
							throw new FormTriggerFailureException(event);
						}

						query = """
								SELECT data_supplier
								           from navdb
								          WHERE UPPER (navdb_id) = UPPER (?) OR UPPER (navdb_id) = UPPER (RPAD (?, 3, ' '))
								""";
						rec = app.selectInto(query, vCust, vCust);
						vsSupplier = rec.getString();
						if (!Objects.equals(vsSupplier, global.getDataSupplier())) {
							if (Objects.equals(vsCustomer, null)) {
								vsCustomer = vCust;
							}

							else {
								vsCustomer = vsCustomer + "," + vCust;
							}
						}

						query = """
								SELECT COUNT (*)
								           from customer
								          WHERE customer_ident = ? OR customer_ident = RPAD (?, 3, ' ')
								""";
						rec = app.selectInto(query, vCust, vCust);
						vCount = rec.getInt();
						if (Objects.equals(vCount, 0)) {
							coreptLib.dspMsg("Invalid Customer Ident '" + vCust + "'. Please correct/delete it.");
							throw new FormTriggerFailureException(event);
						}

						else {
							if (instr("," + vAllCust + ",", "," + vCust + ",") > 0) {
								coreptLib.dspMsg("Customer Ident '" + vCust + "' is duplicated. Please delete one.");
								throw new FormTriggerFailureException(event);
							}

						}
						addGroupRow(groupId, "end_of_group");
						vRow = vRow + 1;
						setGroupCharCell(groupId, "custId", vRow, ltrim(rtrim(vCust)));
					}
				}
			}
			parameter.setValidateCustomer(1);
			parameter.setCustIdents(vsCustomer);

			log.info("validateCustomer Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing validateCustomer" + e.getMessage());
			throw e;

		}
	}

	@Override
	public DuplRecordsDto duplRecords(String pSupplier, String pRecordType, String pNoOfRecords, String pGeninhouse,
			String pOldCustomer, String pNewCustomer, String pTableName, String pRowId, Integer pDcrNumber,
			Integer pFromCycle, Integer pToCycle, Integer pRecentCycle, String pNewCycleData, String p1stIdent,
			String p2ndIdent, String p2ndIcao, String pDetailsExist, String pWhere, String pRefreshMayNeed,
			String pMessage, Integer pNewGateIdent, String pGeninouthouse) throws Exception {
		log.info("duplRecords Executing");
		String query = "";
		Record rec = null;
		DuplRecordsDto dto = new DuplRecordsDto();
		CopyDetailsDto copyDto = new CopyDetailsDto();
		try {
			Map<String, Object> records = null;
			String vStmt = null;
			String vStmt1 = null;
//			String vWhereStmt = null;
			String vMessage = null;
			Integer vNmrec = 0;
			String vTargetTable = null;
			String vsTargetSupplier = null;
			Integer vnRefreshCycle = 0;
			Integer vnRefreshOldCycle = 0;
			Integer vStartDcr = 0;
//			Integer vDcrNum = 0;
			Integer vNminvalid = 0;
//			String vDetail = null;

//			String getDcr = null;
//
//			String vPkFields = null;
//			Integer vCount = 0;
//			Integer vDupCount = 0;
			String vCycleData = null;
			String vAreaCode = null;

			String lsGate = null;
			String lsAirportIdent = null;
			String lsAirportIcao = null;

			Integer lnDcrNum = null;
			String lsOneWhere = null;
			String lsWhere = null;
			String vTableType = null;
			String getDetail = """
					SELECT TABLE_NAME
					        FROM cpt_table_list
					       WHERE PARENT_TABLE = UPPER (SUBSTR (?, 4))
					""";

			try {
				if (Objects.equals(pRecordType, "S")) {
					if (Objects.equals(pNewCustomer, null)) {
						vTargetTable = pTableName;
					} else {
						vTargetTable = "PL_TLD_" + pTableName.substring(8 - 1);
					}
				} else {
					vTargetTable = pTableName;
				}
				if (!Objects.equals(pNewCustomer, null)) {

					query = """
							SELECT data_supplier
							        from navdb
							       WHERE UPPER (navdb_id) = UPPER (?)
							""";
					rec = app.selectInto(query, pNewCustomer);
					vsTargetSupplier = rec.getString();
				} else {
					vsTargetSupplier = pSupplier;
				}

				query = """
						SELECT dcr_number_seq.NEXTVAL from DUAL
						""";
				rec = app.selectInto(query);
				vStartDcr = rec.getInt();

				try {

					query = """
							SELECT 'Y'
							        from all_tab_columns
							       WHERE     owner = 'CPT'
							             AND table_name = UPPER (SUBSTR (?, 4))
							             AND column_name = 'CYCLE_DATA'
							""";
					rec = app.selectInto(query, vTargetTable);
					vCycleData = rec.getString();
				}
				// NO_DATA_FOUND
				catch (NoDataFoundException e) {
					vCycleData = "N";

				}
				if (like("PL_STD%", vTargetTable)) {

					try {

						query = """
								SELECT 'Y'
								           from all_tab_columns
								          WHERE     owner = 'CPT'
								                AND table_name = UPPER (SUBSTR (?, 4))
								                AND column_name = 'AREA_CODE'
								""";
						rec = app.selectInto(query, vTargetTable);
						vAreaCode = rec.getString();
					}
					// NO_DATA_FOUND
					catch (NoDataFoundException e) {
						vAreaCode = "N";
					}

				}

				vStmt = setInsertClause(vTargetTable, vCycleData, vAreaCode) + " "
						+ setSelectClause(pNoOfRecords, pNewCustomer, pTableName, pDcrNumber, pToCycle, p1stIdent,
								p2ndIdent, p2ndIcao, vCycleData, vAreaCode, pNewCycleData, pGeninouthouse);
				if (!OracleHelpers.isNullorEmpty(global.getNewGateIdent())) {

					vStmt1 = vStmt.substring(0, vStmt.indexOf("GATE_IDENT", vStmt.indexOf("GATE_IDENT") + 1)) + "'"
							+ global.getNewGateIdent() + "'"
							+ vStmt.substring(0, vStmt.indexOf("GATE_LATITUDE", vStmt.indexOf("GATE_LATITUDE") + 1));
					vStmt = vStmt1;
				}

				if (Objects.equals(pNoOfRecords, "ALL")) {
					// null;
				}

				else if (Objects.equals(pNoOfRecords, "SET")) {
					// null;
				}

				else {
					vStmt = vStmt + "where rowid = chartorowid('" + pRowId + "') ";
				}

				vNmrec = app.executeNonQuery(vStmt);

				if (Objects.equals(pDetailsExist, "Y") && vNmrec > 0) {
					copyDto = copyDetails(pSupplier, pRecordType, pNoOfRecords, pGeninhouse, pOldCustomer, pNewCustomer,
							vTargetTable, pRowId, pDcrNumber, pFromCycle, pToCycle, p1stIdent, p2ndIdent, p2ndIcao,
							pWhere, pNewCycleData, pGeninouthouse);
				}

				else {
					copyDto.setVMessage("O.K.");

				}
				if (Objects.equals(copyDto.getVMessage(), "O.K.")) {
					if (Objects.equals(pRecordType, "S")) {
						if (Objects.equals(nameIn(this, "parameter.rep"), "N")) {
							if (Objects.equals(pNewCustomer, null)) {
								dto.setPMessage(toChar(vNmrec) + " standard record is copied from the cycle "
										+ pFromCycle + " to cycle " + pToCycle + " ");
							} else {
								dto.setPMessage(toChar(vNmrec) + " record is copied from the cycle " + pFromCycle
										+ " standard record to cycle " + pToCycle + " tailored data with\nID '"
										+ pNewCustomer + "'");
							}
						}

						else if (Objects.equals(nameIn(this, "parameter.rep"), "Y")) {
							throw new EDuplicate();
						}
					}

					else {
						if (Objects.equals(nameIn(this, "parameter.rep"), "N")) {
							if (Objects.equals(pNoOfRecords, "ALL") && Objects.equals(vNmrec, 0)) {
								// null;
							} else {
								dto.setPMessage(toChar(vNmrec) + " record(s) in cycle " + pFromCycle + " with ID '"
										+ pOldCustomer + "' is/are copied to cycle " + pToCycle + " with ID '"
										+ pNewCustomer + "'");
							}
						} else if (Objects.equals(nameIn(this, "parameter.rep"), "Y")) {
							throw new EDuplicate();
						}
					}
					if (Objects.equals(pNoOfRecords, "ONE") && !Objects.equals(p1stIdent, null)
							|| !Objects.equals(p1stIdent, "") && vNmrec > 0) {
						if (Objects.equals(pNewCustomer, null)) {
							dto.setPMessage(dto.getPMessage() + "with data type ident '" + p1stIdent + "'.");
						} else {
							dto.setPMessage(dto.getPMessage() + "and data type ident '" + p1stIdent + "'.");
						}
					}

					if (!Objects.equals(pNoOfRecords, "ONE")) {
						// null;
					}

					else {

						records = app.executeProcedure("CPTS", "strval_copied_records", "VANDV_COPIED_RECORDS",
								new ProcedureInParameter("p_table", vTargetTable.substring(4 - 1), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_cycle", pToCycle, OracleTypes.NUMBER),
								new ProcedureInParameter("p_supplier", vsTargetSupplier, OracleTypes.VARCHAR),
								new ProcedureInParameter("p_start_dcr", vStartDcr, OracleTypes.NUMBER),
								new ProcedureInParameter("p_upd_dcr", pDcrNumber, OracleTypes.NUMBER),

								new ProcedureOutParameter("p_message", OracleTypes.VARCHAR),

								new ProcedureInParameter("p_from_DU", null, OracleTypes.VARCHAR),
								new ProcedureOutParameter("p_run_loc", OracleTypes.VARCHAR),
								new ProcedureInParameter("p_copy", null, OracleTypes.VARCHAR));

						vMessage = records.get("p_message").toString();

						if (Objects.equals(vMessage, "O.K.")) {

							records = app.executeProcedure("CPTS", "relval_copied_records", "VANDV_COPIED_RECORDS",
									new ProcedureInParameter("p_supplier", vsTargetSupplier, OracleTypes.VARCHAR),
									new ProcedureInParameter("p_cycle", pToCycle, OracleTypes.NUMBER),
									new ProcedureInParameter("p_recent_cycle", pRecentCycle, OracleTypes.NUMBER),
									new ProcedureInParameter("p_table", vTargetTable.substring(4), OracleTypes.VARCHAR),
									new ProcedureInParameter("p_start_dcr", vStartDcr, OracleTypes.NUMBER),
									new ProcedureInParameter("p_upd_dcr", pDcrNumber, OracleTypes.NUMBER),
									new ProcedureInParameter("p_master_table", null, OracleTypes.VARCHAR),

									new ProcedureOutParameter("p_message", OracleTypes.VARCHAR),

									new ProcedureInParameter("p_from_DU", null, OracleTypes.VARCHAR),
									new ProcedureInParameter("p_run_loc", null, OracleTypes.VARCHAR),
									new ProcedureInParameter("p_copy", null, OracleTypes.VARCHAR));

							vMessage = records.get("p_message").toString();

							if (Objects.equals(vMessage, "O.K.")) {
								if (Objects.equals(pDetailsExist, "Y")) {
									List<Record> recs = app.executeQuery(getDetail, vTargetTable);
									for (Record record : recs) {
										if (Objects.equals(vMessage, "O.K.")) {
											records = app.executeProcedure("CPTS", "relval_copied_records",
													"VANDV_COPIED_RECORDS",
													new ProcedureInParameter("p_supplier", vsTargetSupplier,
															OracleTypes.VARCHAR),
													new ProcedureInParameter("p_cycle", pToCycle, OracleTypes.NUMBER),
													new ProcedureInParameter("p_recent_cycle", pRecentCycle,
															OracleTypes.NUMBER),
													new ProcedureInParameter("p_table", vTargetTable.substring(4),
															OracleTypes.VARCHAR),
													new ProcedureInParameter("p_start_dcr", vStartDcr,
															OracleTypes.NUMBER),
													new ProcedureInParameter("p_upd_dcr", pDcrNumber,
															OracleTypes.NUMBER),
													new ProcedureInParameter("p_master_table", null,
															OracleTypes.VARCHAR),

													new ProcedureOutParameter("p_message", OracleTypes.VARCHAR),

													new ProcedureInParameter("p_from_DU", null, OracleTypes.VARCHAR),
													new ProcedureInParameter("p_run_loc", null, OracleTypes.VARCHAR),
													new ProcedureInParameter("p_copy", null, OracleTypes.VARCHAR));
											vMessage = records.get("p_message").toString();
										}
									}
								}
							}
						}

						vStmt = "select count(*) from " + vTargetTable + " " + "where create_Dcr_number > " + vStartDcr
								+ " " + "and update_dcr_number = " + pDcrNumber + " " + "and validate_ind = 'I' ";

						records = app.executeProcedure("CPTS", "Exe_Query", "forms_utilities",
								new ProcedureInParameter("p_query", vStmt, OracleTypes.VARCHAR),
								new ProcedureOutParameter("p_err_exist", OracleTypes.NUMBER));
						vNminvalid = Integer.parseInt(records.get("p_err_exist").toString());

						dto.setPMessage(dto.getPMessage() + "!! You\nhave " + vNminvalid
								+ " invalid record(s) in the copied record(s) set !!");

						vnRefreshCycle = app
								.executeFunction(BigDecimal.class, "CPTS", "fetch_rml_cycle", "forms_utilities",
										OracleTypes.NUMBER,
										new ProcedureInParameter("p_supplier", vsTargetSupplier, OracleTypes.VARCHAR))
								.intValue();

						vnRefreshCycle = app
								.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle", "util2",
										OracleTypes.NUMBER,
										new ProcedureInParameter("p_cycle", vnRefreshCycle, OracleTypes.NUMBER))
								.intValue();

						if (Arrays.asList(vnRefreshCycle, vnRefreshOldCycle).contains(pToCycle)
								|| Objects.equals(vNmrec, 0) || Objects.equals(nameIn(this, "parameter.rml"), "R")) {
							dto.setPMayRefreshed("NO");

						}

						else if (Arrays.asList(vnRefreshCycle, vnRefreshOldCycle).contains(pToCycle)) {
							if (Objects.equals(vNmrec, 10)) {
								dto.setPMayRefreshed("TABLE");

							}

							else if (Objects.equals(vNmrec, 1)) {
								vStmt = "select create_dcr_number from " + vTargetTable + " "
										+ " where create_dcr_number > " + vStartDcr + " " + " and update_dcr_number = "
										+ pDcrNumber;

								records = app.executeProcedure("CPTS", "Exe_Query", "forms_utilities",
										new ProcedureInParameter("p_query", vStmt, OracleTypes.VARCHAR),
										new ProcedureOutParameter("p_err_exist", OracleTypes.NUMBER));
								dto.setPMayRefreshed(toString(records.get("p_err_exist")));

							}

						}

					}

				}

				else {
					dto.setPMayRefreshed("NO");
					dto.setPMessage("Duplication is not complete - Error in duplicating details: " + vMessage);
				}
			}

			// e_Duplicate
			catch (EDuplicate e) {

				try {
					if (Objects.equals(pNoOfRecords, "ONE") && Objects.equals(nameIn(this, "parameter.rep"), "N")) {

						if (Objects.equals(pNewCustomer, null)) {
							dto.setPMessage("Record Already Exists in Standard Data");
						} else {
							dto.setPMessage("Record Already Exists for ID: " + pNewCustomer);
						}
					} else {
						if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {

							query = """
									SELECT gate_ident,airport_ident,airport_icao from PL_TLD_GATE
															WHERE rowid = chartorowid (p_row_id)
									""";
							rec = app.selectInto(query);
							lsGate = rec.getString();
							lsAirportIdent = rec.getString();
							lsAirportIcao = rec.getString();
						} else {

							query = """
									SELECT gate_ident,airport_ident,airport_icao from PL_STD_GATE
															WHERE rowid = chartorowid (p_row_id)
									""";
							rec = app.selectInto(query);
							lsGate = rec.getString();
							lsAirportIdent = rec.getString();
							lsAirportIcao = rec.getString();
						}
						if (!Objects.equals(nameIn(this, "global.newGateIdent"), null)) {
							lsOneWhere = "'" + nameIn(this, "global.newGateIdent") + "'@'" + lsAirportIdent + "'@'"
									+ lsAirportIcao + "";
							lsWhere = " GATE_IDENT = '" + nameIn(this, "global.newGateIdent") + "' and airportIdent = "
									+ lsAirportIdent + " and airportIcao = " + lsAirportIcao;
						} else {
							lsOneWhere = "'" + lsGate + "'@'" + lsAirportIdent + "'@'" + lsAirportIcao + "'";
							lsWhere = " GATE_IDENT = '" + lsGate + "' AND AIRPORT_IDENT = '" + lsAirportIdent
									+ "' AND AIRPORT_ICAO = '" + lsAirportIcao + "'";
						}
						if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
							vStmt = "select create_dcr_number from " + vTargetTable + " WHERE data_supplier = '"
									+ vsTargetSupplier + "' AND processing_cycle = " + toChar(pToCycle)
									+ " AND customer_ident = '" + pNewCustomer + "' AND " + lsWhere + " AND "
									+ lsOneWhere + " ||'@'||'" + pSupplier + "'||'@'||" + toChar(pFromCycle)
									+ "||'@'||'" + pOldCustomer + "' IN "
									+ "(SELECT gate_ident||'@'||airport_ident||'@'||airport_icao||'@'||DATA_SUPPLIER||'@'||PROCESSING_CYCLE||'@'||CUSTOMER_IDENT from "
									+ pTableName + " where rowid = chartorowid('" + pRowId + "')) ";
						} else {
							vStmt = "select create_dcr_number from " + vTargetTable + " WHERE data_supplier = '"
									+ vsTargetSupplier + "' AND processing_cycle = " + toChar(pToCycle)
									+ " AND customer_ident = '" + pNewCustomer + "' AND " + lsWhere + " AND "
									+ lsOneWhere + " ||'@'||'" + pSupplier + "'||'@'||" + toChar(pFromCycle) + "' IN "
									+ "(SELECT gate_ident||'@'||airport_ident||'@'||airport_icao||'@'||DATA_SUPPLIER||'@'||PROCESSING_CYCLE from "
									+ pTableName + " where rowid = chartorowid('" + pRowId + "')) ";
						}

						records = app.executeProcedure("CPTS", "Exe_Query", "forms_utilities",
								new ProcedureInParameter("p_query", vStmt, OracleTypes.VARCHAR),
								new ProcedureOutParameter("p_err_exist", OracleTypes.NUMBER));
						lnDcrNum = Integer.parseInt(records.get("p_err_exist").toString());

						vnRefreshCycle = app
								.executeFunction(BigDecimal.class, "CPTS", "fetch_rml_cycle", "forms_utilities",
										OracleTypes.NUMBER,
										new ProcedureInParameter("p_table", vsTargetSupplier, OracleTypes.VARCHAR))
								.intValue();

						vnRefreshOldCycle = app
								.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle", "util2",
										OracleTypes.NUMBER,
										new ProcedureInParameter("p_cycle", vnRefreshCycle, OracleTypes.NUMBER))
								.intValue();

						if (Objects.equals(nameIn(this, "parameter.rml"), "Y")) {
							if (Arrays.asList(vnRefreshCycle, vnRefreshOldCycle).contains(pToCycle)) {
								if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
									refreshMasterLibrary.refreshARecord(vTableType, lnDcrNum, pToCycle, "TLD_GATE", "I",
											vsTargetSupplier);
								} else {
									refreshMasterLibrary.refreshARecord(vTableType, lnDcrNum, pToCycle, "STD_GATE", "I",
											vsTargetSupplier);
								}
							}
						}

						records = app.executeProcedure("CPTS", "Delete_From_Ref_Table", "forms_utilities",
								new ProcedureInParameter("pi_nDcr_Number", lnDcrNum, OracleTypes.NUMBER),
								new ProcedureInParameter("pi_nReferenced_Dcr", null, OracleTypes.NUMBER));

						if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {

							query = """
									 Delete from pl_tld_gate
															WHERE create_dcr_number = ?
									""";
							app.executeNonQuery(query, lnDcrNum);
						}

						else {

							query = """
									 Delete from pl_std_gate
															WHERE create_dcr_number = ?
									""";
							app.executeNonQuery(query, lnDcrNum);
						}
						dto.setPMayRefreshed("NO");
						dto.setPMessage("R");
					}
				}
				// no_data_found
				catch (NoDataFoundException e1) {
					dto.setPMayRefreshed("NO");
					dto.setPMessage("R");
				}

			}

			// DUP_VAL_ON_INDEX -- oracle sql exception
			catch (SQLException e) {
//				if (e.getErrorCode() == 1) {
//
//				}
				try {
					if (Objects.equals(pNoOfRecords, "ONE") && Objects.equals(nameIn(this, "parameter.rep"), "N")) {
						if (Objects.equals(pNewCustomer, null)) {
							dto.setPMessage("Record Already Exists in Standard Data");
						} else {
							dto.setPMessage("Record Already Exists for ID: " + pNewCustomer);
						}
					}

					else {
						if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {

							query = """
									SELECT gate_ident,airport_ident,airport_icao from PL_TLD_GATE
															WHERE rowid = chartorowid (?)
									""";
							rec = app.selectInto(query, pRowId);
							lsGate = rec.getString();
							lsAirportIdent = rec.getString();
							lsAirportIcao = rec.getString();

						}

						else {

							query = """
									SELECT gate_ident,airport_ident,airport_icao from PL_STD_GATE
															WHERE rowid = chartorowid (?)
									""";
							rec = app.selectInto(query, pRowId);
							lsGate = rec.getString();
							lsAirportIdent = rec.getString();
							lsAirportIcao = rec.getString();

						}
						if (!Objects.equals(nameIn(this, "global.newGateIdent"), null)) {
//							 lsOneWhere = "'" + nameIn(this,"global.new_gate_ident") + """||""@""||""" +
//							 lsAirportIdent + """||""@""||""" + lsAirportIcao + """";
//							 lsWhere = " GATE_IDENT = '" + nameIn(this,"global.new_gate_ident") + """ and
//							 airportIdent = """ + lsAirportIdent + """ and airportIcao = """ +
//							 lsAirportIcao + """";

						}

						else {
							lsOneWhere = "'" + lsGate + "'||'@'||'" + lsAirportIdent + "'||'@'||'" + lsAirportIcao
									+ "'";
							lsWhere = " GATE_IDENT = '" + lsGate + "' AND AIRPORT_IDENT = '" + lsAirportIdent
									+ "' AND AIRPORT_ICAO = '" + lsAirportIcao + "'";

						}
						if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
							vStmt = "select create_dcr_number from " + vTargetTable + " WHERE data_supplier = '"
									+ vsTargetSupplier + "' AND processing_cycle = " + toChar(pToCycle)
									+ " AND customer_ident = '" + pNewCustomer + "' AND " + lsWhere + " AND "
									+ lsOneWhere + " ||'@'||'" + pSupplier + "'||'@'||" + toChar(pFromCycle)
									+ "||'@'||'" + pOldCustomer + "' IN "
									+ "(SELECT gate_ident||'@'||airport_ident||'@'||airport_icao||'@'||DATA_SUPPLIER||'@'||PROCESSING_CYCLE||'@'||CUSTOMER_IDENT from "
									+ pTableName + " where rowid = chartorowid('" + pRowId + "')) ";
						}

						else {
							vStmt = "select create_dcr_number from " + vTargetTable + " WHERE data_supplier = '"
									+ vsTargetSupplier + "' AND processing_cycle = " + toChar(pToCycle)
									+ " AND customer_ident = '" + pNewCustomer + "' AND " + lsWhere + " AND "
									+ lsOneWhere + " ||'@'||'" + pSupplier + "'||'@'||" + toChar(pFromCycle) + "' IN "
									+ "(SELECT gate_ident||'@'||airport_ident||'@'||airport_icao||'@'||DATA_SUPPLIER||'@'||PROCESSING_CYCLE from "
									+ pTableName + " where rowid = chartorowid('" + pRowId + "')) ";
						}

						records = app.executeProcedure("CPTS", "Exe_Query", "forms_utilities",
								new ProcedureInParameter("p_query", vStmt, OracleTypes.VARCHAR),
								new ProcedureOutParameter("p_err_exist", OracleTypes.NUMBER));
						lnDcrNum = Integer.parseInt(records.get("p_err_exist").toString());

						vnRefreshCycle = app
								.executeFunction(BigDecimal.class, "CPTS", "fetch_rml_cycle", "forms_utilities",
										OracleTypes.NUMBER,
										new ProcedureInParameter("p_table", vsTargetSupplier, OracleTypes.VARCHAR))
								.intValue();

						vnRefreshOldCycle = app
								.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle", "util2",
										OracleTypes.NUMBER,
										new ProcedureInParameter("p_cycle", vnRefreshCycle, OracleTypes.NUMBER))
								.intValue();

						if (Objects.equals(nameIn(this, "parameter.rml"), "Y")) {
							if (Arrays.asList(vnRefreshCycle, vnRefreshOldCycle).contains(pToCycle)) {
								if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
									refreshMasterLibrary.refreshARecord(vTableType, lnDcrNum, pToCycle, "TLD_GATE", "I",
											vsTargetSupplier);
								} else {
									refreshMasterLibrary.refreshARecord(vTableType, lnDcrNum, pToCycle, "STD_GATE", "I",
											vsTargetSupplier);
								}
							}
						}
						records = app.executeProcedure("CPTS", "Delete_From_Ref_Table", "forms_utilities",
								new ProcedureInParameter("pi_nDcr_Number", lnDcrNum, OracleTypes.NUMBER),
								new ProcedureInParameter("pi_nReferenced_Dcr", null, OracleTypes.NUMBER));

						if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {

							query = """
									 Delete from pl_tld_gate
															WHERE create_dcr_number = ?
									""";
							app.executeNonQuery(query, lnDcrNum);
						}

						else {
							query = """
									 Delete from pl_std_gate
															WHERE create_dcr_number = ?
									""";
							app.executeNonQuery(query, lnDcrNum);
						}

						dto.setPMayRefreshed("NO");
						dto.setPMessage("R");
					}
				} catch (NoDataFoundException e1) {
					dto.setPMayRefreshed("NO");
					dto.setPMessage("R");
				}
			}

			catch (Exception e1) {
//				Integer eCode = OracleHelpers.getSQLcode(e1);
				String msg = OracleHelpers.getSQLerrm(e1);
				if (msg.contains("ORA-00001: unique constraint")) {

					try {
						if (Objects.equals(pNoOfRecords, "ONE") && Objects.equals(nameIn(this, "parameter.rep"), "N")) {

							if (Objects.equals(pNewCustomer, null)) {
								dto.setPMessage("Record Already Exists in Standard Data");
							} else {
								dto.setPMessage("Record Already Exists for ID: " + pNewCustomer);
							}
						} else {
							if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {

								query = """
										SELECT gate_ident,airport_ident,airport_icao from PL_TLD_GATE
																WHERE rowid = chartorowid (?)
										""";
								rec = app.selectInto(query, pRowId);
								lsGate = rec.getString();
								lsAirportIdent = rec.getString();
								lsAirportIcao = rec.getString();
							} else {

								query = """
										SELECT gate_ident,airport_ident,airport_icao from PL_STD_GATE
																WHERE rowid = chartorowid (?)
										""";
								rec = app.selectInto(query, pRowId);
								lsGate = rec.getString();
								lsAirportIdent = rec.getString();
								lsAirportIcao = rec.getString();
							}
							if (!Objects.equals(nameIn(this, "global.newGateIdent"), null)) {
								lsOneWhere = "'" + nameIn(this, "global.newGateIdent") + "'@'" + lsAirportIdent + "'@'"
										+ lsAirportIcao + "";
								lsWhere = " GATE_IDENT = '" + nameIn(this, "global.newGateIdent")
										+ "' and airportIdent = " + lsAirportIdent + " and airportIcao = "
										+ lsAirportIcao;
							} else {
								lsOneWhere = "'" + lsGate + "'@'" + lsAirportIdent + "'@'" + lsAirportIcao + "'";
								lsWhere = " GATE_IDENT = '" + lsGate + "' AND AIRPORT_IDENT = '" + lsAirportIdent
										+ "' AND AIRPORT_ICAO = '" + lsAirportIcao + "'";
							}
							if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
								vStmt = "select create_dcr_number from " + vTargetTable + " WHERE data_supplier = '"
										+ vsTargetSupplier + "' AND processing_cycle = " + toChar(pToCycle)
										+ " AND customer_ident = '" + pNewCustomer + "' AND " + lsWhere + " AND "
										+ lsOneWhere + " ||'@'||'" + pSupplier + "'||'@'||" + toChar(pFromCycle)
										+ "||'@'||'" + pOldCustomer + "' IN "
										+ "(SELECT gate_ident||'@'||airport_ident||'@'||airport_icao||'@'||DATA_SUPPLIER||'@'||PROCESSING_CYCLE||'@'||CUSTOMER_IDENT from "
										+ pTableName + " where rowid = chartorowid('" + pRowId + "')) ";
							} else {
								vStmt = "select create_dcr_number from " + vTargetTable + " WHERE data_supplier = '"
										+ vsTargetSupplier + "' AND processing_cycle = " + toChar(pToCycle)
										+ " AND customer_ident = '" + pNewCustomer + "' AND " + lsWhere + " AND "
										+ lsOneWhere + " ||'@'||'" + pSupplier + "'||'@'||" + toChar(pFromCycle)
										+ "' IN "
										+ "(SELECT gate_ident||'@'||airport_ident||'@'||airport_icao||'@'||DATA_SUPPLIER||'@'||PROCESSING_CYCLE from "
										+ pTableName + " where rowid = chartorowid('" + pRowId + "')) ";
							}

							records = app.executeProcedure("CPTS", "Exe_Query", "forms_utilities",
									new ProcedureInParameter("p_query", vStmt, OracleTypes.VARCHAR),
									new ProcedureOutParameter("p_err_exist", OracleTypes.NUMBER));
							lnDcrNum = Integer.parseInt(records.get("p_err_exist").toString());

							vnRefreshCycle = app
									.executeFunction(BigDecimal.class, "CPTS", "fetch_rml_cycle", "forms_utilities",
											OracleTypes.NUMBER,
											new ProcedureInParameter("p_table", vsTargetSupplier, OracleTypes.VARCHAR))
									.intValue();

							vnRefreshOldCycle = app
									.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle", "util2",
											OracleTypes.NUMBER,
											new ProcedureInParameter("p_cycle", vnRefreshCycle, OracleTypes.NUMBER))
									.intValue();

							if (Objects.equals(nameIn(this, "parameter.rml"), "Y")) {
								if (Arrays.asList(vnRefreshCycle, vnRefreshOldCycle).contains(pToCycle)) {
									if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
										refreshMasterLibrary.refreshARecord(vTableType, lnDcrNum, pToCycle, "TLD_GATE",
												"I", vsTargetSupplier);
									} else {
										refreshMasterLibrary.refreshARecord(vTableType, lnDcrNum, pToCycle, "STD_GATE",
												"I", vsTargetSupplier);
									}
								}
							}

							records = app.executeProcedure("CPTS", "Delete_From_Ref_Table", "forms_utilities",
									new ProcedureInParameter("pi_nDcr_Number", lnDcrNum, OracleTypes.NUMBER),
									new ProcedureInParameter("pi_nReferenced_Dcr", null, OracleTypes.NUMBER));

							if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {

								query = """
										 Delete from pl_tld_gate
																WHERE create_dcr_number = ?
										""";
								app.executeNonQuery(query, lnDcrNum);
							}

							else {

								query = """
										 Delete from pl_std_gate
																WHERE create_dcr_number = ?
										""";
								app.executeNonQuery(query, lnDcrNum);
							}
							dto.setPMayRefreshed("NO");
							dto.setPMessage("R");
						}
					} catch (NoDataFoundException nf) {
						dto.setPMayRefreshed("NO");
						dto.setPMessage("R");
					}

				} else {
					dto.setPMayRefreshed("NO");
					if (!Objects.equals(pNewCustomer, null)) {
						dto.setPMessage("No record is copied from '" + pOldCustomer + "' to '" + pNewCustomer
								+ "' for cycle " + pToCycle + "! --> ");// + e1.get.substring(0,300);
					} else {
						dto.setPMessage("No record is copied from '" + pFromCycle + "' to '" + pToCycle + "'! -->");// +
					}
				}
			}
			// Message msg = new Message();
			msglist.add(dto.getPMessage());
			// message.add(msg);

			log.info("duplRecords Executed Successfully");
			return dto;
		} catch (Exception e) {
			log.error("Error while executing duplRecords" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String getColumn(String pTableName) throws Exception {
		log.info("getColumn Executing");
		String query = "";
//		Record rec = null;
		try {
			String vReturn = "";
			String pSpecialFields = null;
//			Object getColumn = null;
			String vStmt = null;
			String vColumn = "";
			List<Record> records = null;

			try {
				pSpecialFields = " 'AREA_CODE','CUSTOMER_IDENT','GENERATED_IN_HOUSE_FLAG',"
						+ "'FILE_RECNO','CYCLE_DATA','CREATE_DCR_NUMBER',"
						+ "'UPDATE_DCR_NUMBER','PROCESSING_CYCLE','VALIDATE_IND','SUPPLIER_LINE_NUMBER' ";
				if (like("%TLD%", pTableName)) {
					pSpecialFields = pSpecialFields + ",'DATA_SUPPLIER'";

				}

				vStmt = "select column_name from all_tab_columns " + "where  owner = 'CPT' " + "and    table_name = '"
						+ upper(substr(pTableName, 4)) + "' " + "and    column_name not in (" + pSpecialFields
						+ ",'SUPPLIER_LINE_NUMBER') " + "order by column_name";
				if ((like("%TLD%", pTableName))) {
					query = "SELECT column_name FROM all_tab_columns WHERE owner='CPT' AND table_name = UPPER(SUBSTR(?,4)) AND column_name NOT IN ('AREA_CODE','CUSTOMER_IDENT','GENERATED_IN_HOUSE_FLAG','FILE_RECNO','CYCLE_DATA','CREATE_DCR_NUMBER','UPDATE_DCR_NUMBER','PROCESSING_CYCLE','VALIDATE_IND','SUPPLIER_LINE_NUMBER','DATA_SUPPLIER','SUPPLIER_LINE_NUMBER') ORDER BY column_name";
					records = app.executeQuery(query, pTableName);
				}
				// coverity-fixes
//				else {
//					query = "SELECT column_name FROM all_tab_columns WHERE owner='CPT' AND table_name = UPPER(SUBSTR(?,4)) AND column_name NOT IN ('AREA_CODE','CUSTOMER_IDENT','GENERATED_IN_HOUSE_FLAG','FILE_RECNO','CYCLE_DATA','CREATE_DCR_NUMBER','UPDATE_DCR_NUMBER','PROCESSING_CYCLE','VALIDATE_IND','SUPPLIER_LINE_NUMBER','DATA_SUPPLIER','SUPPLIER_LINE_NUMBER') ORDER BY column_name";
//					records = app.executeQuery(query, pTableName);
//				}

				// coverity-fixes
				if (!Objects.equals(records, null)) {
					for (Record record : records) {
						vColumn = record.getString();
						vReturn = vReturn.concat(vColumn).concat(",");
					}
				}
				return vReturn;
			}
			// OTHERS
			catch (Exception e) {
				// vStmt = sqlerrm + "---" + vStmt;
				vStmt = e.getMessage() + "---" + vStmt;
				query = """
						 INSERT INTO test (tstmt) VALUES (?)
						""";
				app.executeNonQuery(query, vStmt);
				log.info("getColumn Executed Successfully");
				return null;
			}

		} catch (Exception e) {
			log.error("Error while executing getColumn" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void duplicateStep2() throws Exception {
		log.info("duplicateStep2 Executing");
		String query = "";
		Record rec = null;

		try {
			String vMessage = null;
			String vTargetTable = null;
			String vTableType = null;
			String vRefreshMayNeed = null;
//			Integer vDcrNumber = 0;
			Integer vLength = 0;
			List<Integer> dcrNumber = new ArrayList<>();
			List<String> newCust = new ArrayList<>();
			Integer i = 0;
			Integer j = -1;
			String vAllCust = null;
			String vNewCust = null;
			String refreshNeed = null;
			String vNewCycleData = null;
			Integer vnSuppExist = 0;
			String vsSupplier = null;
			String vsGlobalSupp = null;
			String vsStmt = null;
			List<String> dataSuppArray = new ArrayList<>();
			Integer vnRefreshNeed = 0;
			Integer vnRefreshNeedTable = 0;
			String vsCopiedCust = null;
			Integer vnCnt = 0;
			List<Integer> dcrTable = new ArrayList<>();
//			Integer lnDcrCount = 0;
			Integer lnDcrNo = 0;
			String lsRepDcr = null;
			String lsNewUpdDcr = null;
			Integer lnLength = 0;
//			Message msg = new Message();

			LocalDateTime currentTime = LocalDateTime.now();
			DateTimeFormatter formate = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
			String time = currentTime.format(formate);
			if (!Objects.equals(parameter.getRepCheck(), "R")) {
				// copy("Start copy " + global.getCurrentTable() + " at - " + time ,
				// "message.runtimeMessage");
				msglist.add("Start copy " + global.getCurrentTable() + " at  " + time);
				// msg.setRuntimeMessage("Start copy " + global.getCurrentTable() + " at " +
				// time);
				// message.add(msg);
			}
			// copy("Start copy " + global.getCurrentTable() + " at - " + time ,
			// "message.runtimeMessage");
			msglist.add("Start copy " + global.getCurrentTable() + " at  " + time);
			// message.add(msg);
//			if (message.size() == 1 && message.getRow(0).getRecordStatus().equals("NEW")) {
//				message.getRow(0).setRuntimeMessage("start copy " + global.getCurrentTable() + " at  " + time);
//				message.getRow(0).setRecordStatus("QUERIED");
//			} else {
//				Message msg = new Message();
//				msg.setRuntimeMessage("start copy " + global.getCurrentTable() + " at  " + time);
//				message.add(msg);
//			}

			if (Objects.equals(global.getLibRefreshed(), "Y")
					&& Arrays.asList(global.getOldProcessingCycle(), global.getNewProcessingCycle())
							.contains(global.getNewCycle())) {
				refreshNeed = "Y";
			} else {
				refreshNeed = "N";
			}
			vAllCust = global.getNewCustomerIdent();
			lsRepDcr = parameter.getRepDcr();
			if (vAllCust != null) {
				while ((!Objects.equals(rtrim(vAllCust), null) && (!Objects.equals(rtrim(vAllCust), "")))) {
					vLength = instr(vAllCust, ",");
					if (Objects.equals(vLength, 0)) {
						vNewCust = vAllCust;
					} else {
						vNewCust = substr(vAllCust, 1, vLength - 1);
					}
					// substring the upto upto 1st , . 1st , not // remov
					vAllCust = substr(vAllCust, length(vNewCust) + 2);
					// newCust.set(i, ltrim(rtrim(vNewCust)));
					newCust.add(ltrim(rtrim(vNewCust)));

					lnLength = instr(lsRepDcr, ",");

					if (Objects.equals(lnLength, 0)) {
						lsNewUpdDcr = lsRepDcr;
//						lsRepDcr = null;
//						dcrTable.add(Integer.parseInt(lsNewUpdDcr));

					} else {
						lsNewUpdDcr = substr(lsRepDcr, 1, lnLength - 1);
					}
					lsRepDcr = substr(lsRepDcr, length(lsNewUpdDcr) + 2);
					dcrTable.add(toInteger(lsNewUpdDcr));
					// dcrTable.set(i, toInteger(lsNewUpdDcr));
					lnDcrNo = dcrTable.get(i);

					if (Objects.equals(newCust.get(i), global.getCurrCustomerIdent())
							&& Objects.equals(global.getNumToDuplicate(), "ALL")
							&& Objects.equals(global.getProcessingCycle(),
									nvl(global.getNewCycle(), global.getProcessingCycle()))) {
						vMessage = "No record is copied from '" + newCust.get(i) + "' to '" + newCust.get(i)
								+ "' for cycle " + global.getProcessingCycle();
					} else {
						if (!Objects.equals(nameIn(this, "global.currCustomerIdent"), newCust.get(i))
								|| !Objects.equals(global.getFirstIdent(), null)
								|| !Objects.equals(global.getSecondIdent(), null)
								|| !Objects.equals(global.getSecondIcao(), null)) {
							vNewCycleData = substr(
									nvl(nameIn(this, "global.newCycle"), nameIn(this, "global.processingCycle")), 3);
						} else {
							vNewCycleData = "OLD";
						}

						query = """
								SELECT data_supplier
											     from navdb
											    WHERE UPPER (navdb_id) = UPPER (?)
								""";

						rec = app.selectInto(query, newCust.get(i));
						vsSupplier = rec.getString();
						if (!Objects.equals(vsSupplier, nameIn(this, "global.dataSupplier"))) {
							vnSuppExist = 0;
							for (String dataSupp : dataSuppArray) {
								if (vsSupplier.equals(dataSupp)) {
									vnSuppExist = 1;
									break;
								}
							}
							if (Objects.equals(vnSuppExist, 0)) {
								dataSuppArray.add(vsSupplier);
							}
						} else {
							// coverity-fixes
							vsGlobalSupp = toString(nameIn(this, "global.dataSupplier"));
							log.info(vsGlobalSupp);

						}

						if (global.getFirstCurrident() == null || global.getSecondIcao() == null) {
							global.setFirstCurrident("");
							global.setSecondCurrIcao("");

						}
						if (global.getSecondCurrident() == null) {
							global.setSecondCurrident("");
						}
						DuplRecordsDto dto = duplRecords(toString(nameIn(this, "global.dataSupplier")),
								toString(nameIn(this, "global.recordType")), global.getNumToDuplicate(),
								global.getGeninhouse(), global.getCurrCustomerIdent(), newCust.get(i),
								global.getCurrentTable(), global.getRowId(), lnDcrNo,
								toInteger(nameIn(this, "global.recordCycle")),
								toInteger(nvl(nameIn(this, "global.newCycle"), nameIn(this, "global.processingCycle"))),
								toInteger(global.getRecentCycle()), vNewCycleData,
								nvl(global.getFirstIdent(), global.getFirstCurrident()),
								nvl(global.getSecondIdent(), global.getSecondCurrident()),
								nvl(global.getSecondIcao(), global.getSecondCurrIcao()), global.getDetailExists(),
								globals.getGLastQuery(), vRefreshMayNeed, vMessage, toInteger(global.getNewGateIdent()), // Integer.parseInt(global.getNewGateIdent()),
								global.getGeninouthouse().toString());
						vRefreshMayNeed = dto.getPMayRefreshed();
						vMessage = dto.getPMessage();
						if (vRefreshMayNeed != null) {
							if (Objects.equals(vRefreshMayNeed, "TABLE")) {
								refreshNeed = "TABLE";
								vnRefreshNeedTable = 1;
							} else if (Objects.equals(vRefreshMayNeed, "NO")) {
								refreshNeed = "N";
							} else {
								while (!Objects.equals(rtrim(vRefreshMayNeed), null)) {
									j = j + 1;
									vnRefreshNeed = 1;
									vLength = instr(vRefreshMayNeed, ",");
									if (Objects.equals(vLength, 0)) {
										dcrNumber.set(j, Integer.parseInt(vRefreshMayNeed));
										break;
									} else {
										dcrNumber.set(j, Integer.parseInt(vRefreshMayNeed.substring(vLength)));
									}
									vRefreshMayNeed = vRefreshMayNeed.substring(dcrNumber.get(j) + 2);
								}

							}

						}
					}
					if (vMessage != null && vRefreshMayNeed != null) {
						if (!Objects.equals(nvl(msglist.get(i), "$$"), nvl(vMessage, "$"))) {
							if (!Objects.equals(vMessage, "R")) {

								// msglist.add(vMessage);
								// message.add(msg);

								if (Objects.equals(vRefreshMayNeed, "NO")) {

									if (Objects.equals(vMessage.substring(0, 1), "0")) {
										// null;
									} else {

										msglist.add(
												"The RML operation has not been performed for the data supplier. the master library will not be refreshed for "
														+ newCust.get(i) + ".");
										// message.add(msg);;
									}
									vnCnt = 1;
								}

								if (Objects.equals(vnCnt, 1)) {
									vnCnt = 0;
								} else {
									vsCopiedCust = vsCopiedCust + "," + newCust.get(i);
								}
							}
						}
					}
					i = i + 1;
				}
				if (Objects.equals(vMessage, "R")) {
					copy("r", "parameter.repCheck");
					refreshNeed = "N";
				} else {
					copy("d", "parameter.repCheck");
				}
				vsCopiedCust = ltrim(vsCopiedCust, ",");
				vAllCust = "END";
			}

			else {

				if (global.getFirstCurrident() == null) {
					global.setFirstCurrident("");
				}
				if (global.getSecondCurrident() == null) {
					global.setSecondCurrident("");
				}
				DuplRecordsDto dto = duplRecords(global.getDataSupplier(), global.getRecordType(),
						global.getNumToDuplicate(), global.getGeninhouse(), global.getCurrCustomerIdent(), null,
						global.getCurrentTable(), global.getRowId(), toInteger(global.getDcrNumber()),
						toInteger(global.getRecordCycle()),
						toInteger(nvl(global.getNewCycle(), global.getProcessingCycle())),
						Integer.parseInt(global.getRecentCycle()), "OLD",
						nvl(global.getFirstIdent(), global.getFirstCurrident()),
						nvl(global.getSecondIdent(), global.getSecondCurrident()),
						nvl(global.getSecondIcao(), global.getSecondCurrIcao()), global.getDetailExists(),
						globals.getGLastQuery(), vRefreshMayNeed, vMessage, toInteger(global.getNewGateIdent()),
						global.getGeninhouse());
				vRefreshMayNeed = dto.getPMayRefreshed();
				vMessage = dto.getPMessage();

				// coverity-fixes
				vsGlobalSupp = toString(nameIn(this, "global.dataSupplier"));
				log.info(vsGlobalSupp);

//				nameIn(this, "global.dataSupplier").toString();
				if (Objects.equals(vRefreshMayNeed, "NO")) {
					refreshNeed = "N";
				}

				else {
					if (Objects.equals(vRefreshMayNeed, "TABLE")) {
						refreshNeed = "TABLE";
						vnRefreshNeedTable = 1;
					}

					else {
						while (!Objects.equals(rtrim(vRefreshMayNeed), null)) {
							j = j + 1;
							vnRefreshNeed = 1;
							vLength = instr(vRefreshMayNeed, ",");
							if (Objects.equals(vLength, 0)) {
								dcrNumber.set(j, Integer.parseInt(vRefreshMayNeed));
								break;
							} else {
								dcrNumber.set(j, Integer.parseInt(vRefreshMayNeed.substring(vLength - 1)));
							}
							// Coverity-fixes
							vRefreshMayNeed = substr(vRefreshMayNeed, 0);
						}
					}
				}

				if (!Objects.equals(vMessage, "R")) {
					if (Objects.equals(vRefreshMayNeed, "NO")) {

						nextRecord("");
						msglist.add(
								"The RML Operation has not been performed for the data supplier.\nThe master library will not be refreshed for "
										+ nvl(nameIn(this, "global.newCycle"), nameIn(this, "global.processingCycle"))
										+ ".");
						// message.add(msg);;
						vnCnt = 1;
					}
				}
			}

			if (!Objects.equals(nameIn(this, "parameter.repCheck"), "R")) {

				nextRecord("");
				msglist.add("End copy " + nameIn(this, "global.currentTable") + " at  " + time);
				// message.add(msg);
			}

			if (Objects.equals(refreshNeed, "N") && Objects.equals(nameIn(this, "parameter.repCheck"), "R")) {
				parameter.setRep("n");
				duplicateStep2();
			} else {
				if (Objects.equals(vnRefreshNeedTable, 1)) {
					if (Objects.equals(nameIn(this, "global.recordType"), "T")) {
						nextRecord("");

						msglist.add("More than 10 records were copied from " + nameIn(this, "global.currCustomerIdent")
								+ ".the master library will not be refreshed.");
						// message.add(msg);;
					}

					else {
						nextRecord("");
						msglist.add("More than 10 records were copied from " + nameIn(this, "global.recordCycle")
								+ ".the master library will not be refreshed.");
						// message.add(msg);;
					}
				}

				else {
					if (Objects.equals(vnRefreshNeed, 1)) {
						nextRecord("");
						if (Objects.equals(nameIn(this, "global.recordType"), "S") && !Objects.equals(vAllCust, null)) {
							vTargetTable = upper("TLD_" + nameIn(this, "global.currentTable").toString());

						}

						else {
							vTargetTable = upper(nameIn(this, "global.currentTable").toString());

						}
						if (Objects.equals(vnRefreshNeed, 1)) {

							msglist.add("The master library will be refreshed with the valid records for "
									+ vsCopiedCust + ".");
							// message.add(msg);

							for (int k = 1; k <= j; k++) {
								if (Pattern.compile(".*TLD.*").matcher(vTargetTable).matches()) {
									query = "SELECT data_supplier FROM navdb WHERE UPPER (navdb_id) = ( SELECT UPPER(CUSTOMER_IDENT) FROM PL_"
											+ vTargetTable + " WHERE CREATE_DCR_NUMBER = " + dcrNumber.get(k) + ")";
									Map<String, Object> result = app.executeProcedure("CPTS", "Exe_Query",
											"forms_utilities",
											new ProcedureInParameter("p_query", vsStmt, OracleTypes.VARCHAR),
											new ProcedureOutParameter("p_err_exist", OracleTypes.NUMBER));
									vsSupplier = result.get("p_err_exist").toString();

								}

								else {
									vsSupplier = nameIn(this, "global.dataSupplier").toString();
								}
								refreshMasterLibrary.refreshARecord(vTableType, dcrNumber.get(k),
										Integer.parseInt(nameIn(this, "global.newCycle").toString()), vTargetTable, "Y",
										vsSupplier);
							}
						}
						msglist.add("Refresh record(s) succesfully ended at  " + time);
						// message.add(msg);
					}
				}
			}

			hideWindow("DUPL");

			for (i = 0; i <= msglist.size() - 1;) {
				message.getRow(i).setRuntimeMessage(msglist.get(i));
				message.add(new Message());
				i++;

			}

			log.info("duplicateStep2 Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing duplicateStep2" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String setInsertClause(String pTableName, String pCycleData, String pAreaCode) throws Exception {
		log.info("setInsertClause Executing");
//		String query = "";
//		Record rec = null;
		try {
			String vReturn = null;
//			String vTable = null;

			vReturn = "insert into " + pTableName + " (";
//			v_return := v_return || get_column (p_table_name);
			vReturn = vReturn + getColumn(pTableName);

			if (like("PL_TLD%", pTableName)) {
				vReturn = vReturn + "customer_ident,generated_in_house_flag,"
						+ "create_dcr_number,update_dcr_number,processing_cycle";
			} else {
				if (Objects.equals(pAreaCode, "Y")) {
					vReturn = vReturn + "area_code,create_dcr_number,update_dcr_number,processing_cycle";
				} else {
					vReturn = vReturn + "create_dcr_number,update_dcr_number,processing_cycle";
				}
			}
			if (!(like("%SEGMENT", pTableName) && (!like("%AIRWAY_SEGMENT", pTableName))
					|| Objects.equals(pTableName, "PL_TLD_ADDL_ALT_DEST"))) {
				vReturn = vReturn + ",VALIDATE_IND";
			}
			vReturn = vReturn + ",DATA_SUPPLIER";
			if (Objects.equals(pCycleData, "Y")) {
				vReturn = vReturn + ",CYCLE_DATA)";
			} else {
				vReturn = vReturn + ")";
			}
			log.info("setInsertClause Executed Successfully");
			return vReturn;
		} catch (Exception e) {
			log.error("Error while executing setInsertClause" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String setSelectClause(String pNoOfRecords, String pNewCustomer, String pTableName, Integer pDcrNumber,
			Integer pToCycle, String p1stIdent, String p2ndIdent, String p2ndIcao, String pCycleData, String pAreaCode,
			String pNewCycleData, String pGeninouthouse) throws Exception {
		log.info("setSelectClause Executing");
		String query = "";
		Record rec = null;
		try {
			String vReturn = null;
//			String vTable = null;
			String vsNewSupplier = null;

			vReturn = "select  ";
			vReturn = vReturn + getColumn(pTableName);
			if (!Objects.equals(rtrim(pNewCustomer), null)) {
				if (Objects.equals(pGeninouthouse, "Y")) {
					vReturn = vReturn + "'" + pNewCustomer + "','Y'," + "dcr_number_seq.nextval," + toChar(pDcrNumber)
							+ "," + toChar(pToCycle);
				} else {
					vReturn = vReturn + "'" + pNewCustomer + "'," + "GENERATED_IN_HOUSE_FLAG,dcr_number_seq.nextval,"
							+ toChar(pDcrNumber) + "," + toChar(pToCycle);
				}
			} else {
				if (Objects.equals(pAreaCode, "Y")) {
					vReturn = vReturn + "area_code,dcr_number_seq.nextval," + toChar(pDcrNumber) + ","
							+ toChar(pToCycle);
				} else {
					vReturn = vReturn + "dcr_number_seq.nextval," + toChar(pDcrNumber) + "," + toChar(pToCycle);
				}
			}
			if (!(like("%SEGMENT", pTableName) && (!like("%AIRWAY_SEGMENT", pTableName))
					|| Objects.equals(pTableName, "PL_TLD_ADDL_ALT_DEST"))) {
				vReturn = vReturn + ",'I'";
			}
			if (!Objects.equals(pNewCustomer, null)) {
				query = """
						SELECT data_supplier
						        from NAVDB
						       WHERE UPPER (navdb_id) = UPPER (?)
						""";
				rec = app.selectInto(query, pNewCustomer);
				vsNewSupplier = rec.getString();
				vReturn = vReturn + ",'" + vsNewSupplier + "'";
			} else {
				vReturn = vReturn + ", DATA_SUPPLIER ";
			}
			if (Objects.equals(pCycleData, "Y")) {
				if (Objects.equals(pNewCycleData, "OLD")) {
					vReturn = vReturn + ", CYCLE_DATA ";
				} else {
					vReturn = vReturn + ",'" + pNewCycleData + "'";
				}
			}
			vReturn = vReturn + " from " + pTableName + " ";
			log.info("setSelectClause Executed Successfully");
			return vReturn;
		} catch (Exception e) {
			log.error("Error while executing setSelectClause" + e.getMessage());
			throw e;

		}
	}

	@Override
	public CopyDetailsDto copyDetail(String pSupplier, String pRecordType, String pNoOfRecords, String pGeninhouse,
			String pOldCustomer, String pNewCustomer, String pMasterToTable, String pDetailToTable, String pRowId,
			Integer pDcrNumber, Integer pFromCycle, Integer pToCycle, String p1stIdent, String p2ndIdent,
			String p2ndIcao, String pWhere, String pNewCycleData, String pGeninouthouse) throws Exception {
		log.info("copyDetail Executing");
		String query = "";
		Record rec = null;
		CopyDetailsDto dto = new CopyDetailsDto();
		try {
			String vStmt = null;
			String vMessage = null;
//			String vDetailFromTable = null;
//			String vMasterFromTable = null;
//			String vCycleData = null;
//			String vAreaCode = null;

			try {
				if (Objects.equals(pRecordType, "S")) {
					dto.setVDetailFromTable("PL_STD_" + substr(pDetailToTable, 8));
					dto.setVMasterFromTable("PL_STD_" + substr(pMasterToTable, 8));
				}

				else {
					dto.setVDetailFromTable(pDetailToTable);
					dto.setVMasterFromTable(pMasterToTable);
				}

				try {

					query = """
							select 'Y' from   all_tab_columns
							      	where  owner = 'CPT'
							      	and    table_name = upper(substr(?,4))
							      	and    column_name = 'CYCLE_DATA'
							""";
					rec = app.selectInto(query, pDetailToTable);
					dto.setVCycleData(rec.getString());
				} catch (NoDataFoundException e) {
					dto.setVCycleData("N");
				}
				if (like("PL_STD%", pDetailToTable)) {

					try {

						query = """
								select 'Y' from   all_tab_columns
								      		where  owner = 'CPT'
								      		and    table_name = upper(substr(?,4))
								      		and    column_name = 'AREA_CODE'
								""";
						rec = app.selectInto(query, pDetailToTable);
						dto.setVAreaCode(rec.getString());
					} catch (NoDataFoundException e) {
						dto.setVAreaCode("N");
					}
				}
				if (pDetailToTable != null) {
					vStmt = setInsertClause(pDetailToTable, dto.getVCycleData(), dto.getVAreaCode()) + " "
							+ setSelectClause(pNoOfRecords, pNewCustomer, dto.getVDetailFromTable(), pDcrNumber,
									pToCycle, p1stIdent, p2ndIdent, p2ndIcao, dto.getVCycleData(), dto.getVAreaCode(),
									pNewCycleData, pGeninouthouse)
							+ " "
							+ setWhereClause(pSupplier, dto.getVMasterFromTable(), pDetailToTable,
									toString(pOldCustomer), pNewCustomer, pRowId, pNoOfRecords, pRecordType,
									pGeninhouse, pFromCycle, pToCycle, pWhere);
					app.executeNonQuery(vStmt);
				}
				log.info("copyDetail Executed Successfully");
				dto.setVMessage("O.K.");
			}
			// others
			catch (Exception e) {
				dto.setVMessage(e.getMessage().substring(0, 300));
				query = """
						 insert into test(ttable,tstmt,errtxt) values(?,?,'Details: '||?)
						""";
				app.executeNonQuery(query, pDetailToTable, vStmt, vMessage);// TODOcommit
				return dto;

			}

			return dto;

		} catch (Exception e) {
			log.error("Error while executing copyDetail" + e.getMessage());
			throw e;

		}
	}

	@Override
	public CopyDetailsDto copyDetails(String pSupplier, String pRecordType, String pNoOfRecords, String pGeninhouse,
			String pOldCustomer, String pNewCustomer, String pTargetTable, String pRowId, Integer pDcrNumber,
			Integer pFromCycle, Integer pToCycle, String p1stIdent, String p2ndIdent, String p2ndIcao, String pWhere,
			String pNewCycleData, String pGeninouthouse) throws Exception {
		log.info("copyDetails Executing");
//		String query = "";
//		Record rec = null;
		pGeninouthouse = "Y";
		try {
			String vDetailToTable = null;
//			String vMessage = null;

			CopyDetailsDto dto = copyDetail(pSupplier, pRecordType, pNoOfRecords, pGeninhouse, pOldCustomer,
					pNewCustomer, pTargetTable, vDetailToTable, pRowId, pDcrNumber, pFromCycle, pToCycle, p1stIdent,
					p2ndIdent, p2ndIcao, pWhere, pNewCycleData, pGeninouthouse);
			if (dto.getVDetailsToTable() != null) {
				if (Pattern.compile(".*SEGMENT$").matcher(dto.getVDetailsToTable()).matches()
						&& Objects.equals(dto.getVMessage(), "O.K.")) {
					dto.setVDetailsToTable(rtrim(dto.getVDetailFromTable(), "SEGMENT") + "LEG");
					dto = copyDetail(pSupplier, pRecordType, pNoOfRecords, pGeninhouse, pOldCustomer, pNewCustomer,
							pTargetTable, dto.getVDetailsToTable(), pRowId, pDcrNumber, pFromCycle, pToCycle, p1stIdent,
							p2ndIdent, p2ndIcao, pWhere, pNewCycleData, pGeninouthouse);
				}
			}

			log.info("copyDetails Executed Successfully");
			return dto;

		} catch (Exception e) {
			log.error("Error while executing copyDetails" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String setWhereClause(String pSupplier, String pMFromTable, String pDToTable, String pOldCustomer,
			String pNewCustomer, String pRowId, String pRecords, String pRecordType, String pGeninhouse,
			Integer pFromCycle, Integer pToCycle, String pWhere) throws Exception {
		log.info("setWhereClause Executing");
//		String query = "";
//		Record rec = null;
		try {
			String vReturn = null;
//			String vPkFields = null;

			if (Objects.equals(pRecords, "ALL")) {
				// null;
			} else {
				vReturn = vReturn + "||data_supplier||processing_cycle";
				if (Objects.equals(pRecordType, "T")) {
					vReturn = vReturn + "||customer_ident";
				}
				if (Objects.equals(pRecords, "ONE")) {
					vReturn = " where " + vReturn + " = (select " + vReturn + " from " + pMFromTable
							+ " where rowid = chartorowid('" + pRowId + "'))";
				}
				// coverity-fixes
//				else if (Objects.equals(pRecords, "SET")) {
//					// null;
//				}
			}
			log.info("setWhereClause Executed Successfully");
			return vReturn;
		} catch (Exception e) {
			log.error("Error while executing setWhereClause" + e.getMessage());
			throw e;
		}
	}

	@Override
	public String pkFields(String pFront, String pTable, String pEnd) throws Exception {
		log.info("pkFields Executing");
//		String query = "";
//		Record rec = null;
		try {
			String vReturn = "";
//			String getColumns = """
//					select p_front||column_name||p_end col
//					      from   cpt_pl_pk_columns
//					      where  table_name = upper(p_table)
//					      and    column_name not in   ('DATA_SUPPLIER','PROCESSING_CYCLE','CUSTOMER_IDENT')
//					""";

//			vReturn = null;
//			List<Record> records = app.executeQuery(getColumns);
//			for (Record rec : records) {
//				vReturn = vReturn + rec.getObject("col");
//			}
//			vReturn = vReturn.substring(0, length(vReturn) - length(pEnd));

			log.info("pkFields Executed Successfully");
			return vReturn;

		} catch (Exception e) {
			log.error("Error while executing pkFields" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void disableMenuItems() throws Exception {
		log.info("disableMenuItems Executing");
//		String query = "";
//		Record rec = null;
		try {

			setMenuItemProperty(rtrim("action") + "." + ltrim("save"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("field") + "." + ltrim("clear"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("field") + "." + ltrim("duplicate"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("scrollUp"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("scrollDown"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("insert"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("remove"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("duplicate"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("clear"), ENABLED, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("mainMenu") + "." + ltrim("edit"), VISIBLE, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("mainMenu") + "." + ltrim("block"), VISIBLE, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("mainMenu") + "." + ltrim("query"), VISIBLE, BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("mainMenu") + "." + ltrim("tools"), VISIBLE, BPROPERTY_FALSE);

			log.info("disableMenuItems Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing disableMenuItems" + e.getMessage());
			throw e;
		}
	}

	@Override
	public void replaceCustPrc() throws Exception {
		log.info("replaceCustPrc Executing");
//		String query = "";
//		Record rec = null;
		try {

			parameter.setRepCust(null);
			parameter.setRepDcr(null);
			goBlock("custListBlk", "customer");

			Integer index = 0;
			while (index < custListBlk.size()) {
				if (index == 0) {
					if (!Objects.equals(custListBlk.getRow(index).getCustomer(), "")
							&& !Objects.equals(custListBlk.getRow(index).getCustomer(), null))
						parameter.setRepCust(custListBlk.getRow(index).getCustomer() + ",");
					if (!Objects.equals(toString(custListBlk.getRow(index).getDcrNumber()), "")
							&& !Objects.equals(custListBlk.getRow(index).getDcrNumber(), null))
						parameter.setRepDcr(custListBlk.getRow(index).getDcrNumber() + ",");
				} else {
					if (parameter.getRepCust() != null) {
						parameter.setRepCust(parameter.getRepCust() + custListBlk.getRow(index).getCustomer() + ",");
						if (!Objects.equals(custListBlk.getRow(index).getDcrNumber(), null))
							parameter.setRepDcr(
									parameter.getRepDcr() + toChar(custListBlk.getRow(index).getDcrNumber()) + ",");
						else
							parameter.setRepDcr(
									parameter.getRepDcr() + toChar(custListBlk.getRow(0).getDcrNumber()) + ",");
					}

				}
				index++;
			}

			if (!Objects.equals(parameter.getRepCust(), null))
				parameter.setRepCust(rtrim(parameter.getRepCust(), ","));
			if (!Objects.equals(parameter.getRepDcr(), null))
				parameter.setRepDcr(rtrim(parameter.getRepDcr(), ","));

			log.info("replaceCustPrc Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing replaceCustPrc" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void checkBlankDcrPrc() throws Exception {
		log.info("checkBlankDcrPrc Executing");
		String query = "";
		Record rec = null;
		try {
			Integer lnCount = 0;
			Integer lnDcr = 0;
//			Boolean lsReturn = null;

			// coverity-fixes
			Boolean lsReturn = true;
			Integer lnOldCycle = null;
			Integer lnMaxCycle = null;
			Integer lnMaxCycle1 = null;
			Integer lnMaxCycle2 = null;
			Integer lnNextCycle = 0;
			Integer lnProcCycle = 0;
			Integer index = 0;

			try {

				try {

					query = """
							SELECT UPPER(Instance_Type) from cptcontrol.system_software_identification
							""";
					rec = app.selectInto(query);
					parameter.setLsInstanceName(rec.getString());

				} catch (Exception e) {
					parameter.setLsInstanceName("DEVE");
				}
				if (!Objects.equals(parameter.getLsInstanceName(), "ENGR")) {
					goBlock("custListBlk", "customer");
					if (custListBlk.getRow(custListBlk.size() - 1).getCustomer() == null) {
						custListBlk.remove(custListBlk.size() - 1);
					}
					if (custListBlk.size() != 1) {
						for (int i = 0; i <= custListBlk.size() - 1; i++) {
							if (Objects.equals(custListBlk.getRow(i).getDcrNumber(), null)
									&& Objects.equals(custListBlk.getRow(i).getCustomer(), null)) {
								custListBlk.remove(i);
							}
						}
					}
					while (index < custListBlk.size()) {
						if (Objects.equals(custListBlk.getRow(index).getDcrNumber(), null)
								&& Objects.equals(custListBlk.getRow(index).getCustomer(), null)) {
							custListBlk.remove(index);
						}
						if (Objects.equals(custListBlk.getRow(index).getDcrNumber(), null)) {
							coreptLib.dspMsg(
									"DCR number is Blank for Customer " + custListBlk.getRow(index).getCustomer()
											+ ". Please enter/select\nthe DCR number from the List");
							throw new FormTriggerFailureException();
							// break;
						}

						else {
							parameter.setProcessingCycle(dupl.getNewCycle());

							query = """
									SELECT MIN(CYCLE)
									           from CYCLE
									          WHERE CYCLE > To_Number(?)
									""";
							rec = app.selectInto(query, dupl.getNewCycle());
							lnProcCycle = rec.getInt();

							query = """
									SELECT COUNT(*) from Search_by_Navdb_Assignee
													WHERE NavDB_ID = ?
													AND Effectivity_cycle IN (util2.get_previous_cycle(?), ?,?)
													AND DCR_overall_status = 'OPEN'
													AND DCR_Number = ?
									""";
							rec = app.selectInto(query, custListBlk.getRow(index).getCustomer(), dupl.getNewCycle(),
									dupl.getNewCycle(), lnProcCycle, custListBlk.getRow(index).getDcrNumber());
							lnCount = rec.getInt();

							if (Objects.equals(lnCount, 0)) {
								coreptLib.dspMsg("DCR number# " + custListBlk.getRow(index).getDcrNumber()
										+ " is not valid with respect to\nCustomer "
										+ custListBlk.getRow(index).getCustomer() + " for cycle " + dupl.getNewCycle());

								// below if done manyally ,need to verify again.
								if (Objects.equals(lnCount, 0)) {
									throw new FormTriggerFailureException(event);
									// break;
								}

								query = """
										select max(processing_cycle)
										            from pl_std_airport
										           where data_supplier = ?
										""";
								rec = app.selectInto(query, global.getDataSupplier());
								lnMaxCycle1 = rec.getInt();

								query = """
										select max(processing_cycle)
										            from pl_tld_airport
										           where data_supplier = ?
										""";
								rec = app.selectInto(query, global.getDataSupplier());
								lnMaxCycle2 = rec.getInt();
								if (lnMaxCycle1 > lnMaxCycle2) {
									lnMaxCycle = lnMaxCycle1;
								}

								else if (lnMaxCycle2 > lnMaxCycle1) {
									lnMaxCycle = lnMaxCycle2;
								}

								else {
									lnMaxCycle = lnMaxCycle2;
								}
								if (dupl.getNewCycle() > lnMaxCycle) {
									coreptLib.dspMsg(
											"Copying is not allowed, Cycle#" + dupl.getNewCycle() + " does not Exist ");
									// raise err_no
									throw new FormTriggerFailureException(event);
								}

								if (dupl.getNewCycle() > Integer.parseInt(global.getProcessingCycle())) {

									query = """
											select NVL(min(processing_cycle),0)  -- gives next cycle
											                 from pl_std_airport
													            where data_supplier = ?
											                  and processing_cycle > ?
											""";
									rec = app.selectInto(query, global.getDataSupplier(), global.getProcessingCycle());
									lnNextCycle = rec.getInt();
									if (Objects.equals(lnNextCycle, 0)) {

										query = """
												select NVL(min(processing_cycle),0)  -- gives next cycle
												                    from pl_tld_airport
														               where data_supplier = ?
												                     and processing_cycle > ?
												""";
										rec = app.selectInto(query, global.getDataSupplier(),
												global.getProcessingCycle());
										lnNextCycle = rec.getInt();
									}

									if (dupl.getNewCycle() > lnNextCycle) {
										coreptLib.dspMsg("No DCR exists for " + custListBlk.getRow(index).getCustomer()
												+ " in Target Cycle " + dupl.getNewCycle());
										throw new FormTriggerFailureException(event);
									}

								}

								else if (dupl.getNewCycle() < Integer.parseInt(global.getProcessingCycle())) {
									Map<String, Object> values = app.executeProcedure("CPT", "get_previous_cycle",
											"util2", global.getProcessingCycle());
									lnOldCycle = Integer.parseInt(values.get("v_pre_cycle").toString());
									if (dupl.getNewCycle() < lnOldCycle) {
										coreptLib.dspMsg("No DCR exists for " + custListBlk.getRow(index).getCustomer()
												+ " in Target Cycle " + dupl.getNewCycle());
										throw new FormTriggerFailureException(event);
									}
								}

								try {
									parameter.setProcessingCycle(dupl.getNewCycle());

									query = """
											SELECT DCR_Number from Search_by_Navdb_Assignee
														     WHERE NavDB_ID = ?
													   	     AND Effectivity_cycle = ?
														       AND DCR_overall_status = 'OPEN'
											""";
									rec = app.selectInto(query, custListBlk.getRow(index).getCustomer(),
											dupl.getNewCycle());
									lnDcr = rec.getInt();
								} catch (NoDataFoundException e) {
									if (Integer.parseInt(global.getProcessingCycle()) < dupl.getNewCycle()) {
										parameter.setProcessingCycle(Integer.parseInt(global.getProcessingCycle()));

										try {

											query = """
													SELECT DCR_Number from Search_by_Navdb_Assignee
																           WHERE NavDB_ID = ?
																             AND Effectivity_cycle = ?--ln_old_cycle--?
																             AND DCR_overall_status = 'OPEN'
													""";
											rec = app.selectInto(query, custListBlk.getRow(index).getCustomer(),
													global.getProcessingCycle());
											lnDcr = rec.getInt();
										} catch (NoDataFoundException e2) {
											throw new FormTriggerFailureException(event);
										}

									}

									else if (Integer.parseInt(global.getProcessingCycle()) > dupl.getNewCycle()) {
										parameter.setProcessingCycle(Integer.parseInt(global.getProcessingCycle()));

										try {

											query = """
													SELECT DCR_Number from Search_by_Navdb_Assignee
																           WHERE NavDB_ID = ?
																            AND Effectivity_cycle = ?
																            AND DCR_overall_status = 'OPEN'
													""";
											rec = app.selectInto(query, custListBlk.getRow(index).getCustomer(),
													global.getProcessingCycle());
											lnDcr = rec.getInt();
										} catch (NoDataFoundException e2) {
											throw new FormTriggerFailureException("NO_DATA_FOUND");
										}
									}

									else if (Objects.equals(toInteger(global.getProcessingCycle()),
											dupl.getNewCycle())) {
										if (Objects.equals(lnOldCycle, null)) {
											Map<String, Object> values = app.executeProcedure("CPT",
													"get_previous_cycle", "util2", global.getProcessingCycle());
											lnOldCycle = Integer.parseInt(values.get("v_pre_cycle").toString());
										}

										try {
											parameter.setProcessingCycle(lnOldCycle);

											query = """
													SELECT DCR_Number from Search_by_Navdb_Assignee
																           WHERE NavDB_ID = ?
																             AND Effectivity_cycle = ?
																             AND DCR_overall_status = 'OPEN'
													""";
											rec = app.selectInto(query, custListBlk.getRow(index).getCustomer(),
													lnOldCycle);
											lnDcr = rec.getInt();
										} catch (NoDataFoundException e2) {
											if (Objects.equals(lnNextCycle, 0)) {

												query = """
														SELECT MIN(CYCLE)
														                       from CYCLE
														                      WHERE CYCLE > ?
														""";
												rec = app.selectInto(query, global.getProcessingCycle());
												lnNextCycle = rec.getInt();

											}

											if (Objects.equals(lnNextCycle, 0)) {
												throw new FormTriggerFailureException("NO_DATA_FOUND");
											}

											else {
												parameter.setProcessingCycle(lnNextCycle);
											}

											query = """
													SELECT DCR_Number from Search_by_Navdb_Assignee
																              WHERE NavDB_ID = ?
																                AND Effectivity_cycle = ?
																                AND DCR_overall_status = 'OPEN'
													""";
											rec = app.selectInto(query, custListBlk.getRow(index).getCustomer(),
													lnNextCycle);
											lnDcr = rec.getInt();
										}
									}
								}
								if (!Objects.equals(lnDcr, custListBlk.getRow(index).getDcrNumber())) {
									coreptLib.dspMsg("DCR number# " + custListBlk.getRow(index).getDcrNumber()
											+ " is not valid with respect to Customer "
											+ custListBlk.getRow(index).getCustomer() + " for cycle "
											+ dupl.getNewCycle());
									custListBlk.getRow(index).setDcrNumber(null);
									throw new FormTriggerFailureException(event);
								}
							}
						}
						index++;
					}
				} else {
					custListBlk.getRow(0).setDcrNumber(Integer.parseInt(global.getDcrNumber()));
				}
			} catch (NoDataFoundException e) {
				coreptLib.dspMsg("DCR number# " + custListBlk.getRow(index).getDcrNumber()
						+ " is not valid with respect to\nCustomer " + custListBlk.getRow(index).getCustomer()
						+ " for cycle " + dupl.getNewCycle());
				throw new FormTriggerFailureException();
			}

			// Handled there itself
//			// err_no
//			catch (Exception e) {
//				throw new FormTriggerFailureException();
//			}
//			// form_trigger_failure
			catch (FormTriggerFailureException e) {
				throw e;
			}

			catch (Exception e) {
				if (!lsReturn) {
					coreptLib.dspMsg("Please select a DCR from the List.");
					throw new FormTriggerFailureException(event);
				}
			}
			log.info("checkBlankDcrPrc Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing checkBlankDcrPrc" + e.getMessage());
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> whenNewFormInstance(
        DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
      log.info(" whenNewFormInstance Executing");
      BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
      DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
      try {
        OracleHelpers.bulkClassMapper(reqDto, this);
        String vErr = null;
//			String query = null;
//			Record rec = null;
        vErr = coreptLib.setRole(global.getAllroles());
        if (!Objects.equals(vErr, "PASSED")) {
          oneButton("S", "Fatal Error",
              "The roles cannot be activated. Contact the COREPT Administrator.");
          exitForm();
          OracleHelpers.ResponseMapper(this, resDto);
          return responseObj
              .render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
        }

        // global.setNewCustomerIdent(null);
        // global.setNewGateIdent(null);
        // global.setNewCustomerIdent(null);
        // global.setNumToDuplicate(null);
        // global.setDoDuplicate(null);
        // global.setCurrCustomerIdent(null);
        // global.set_1stCurrident(null);
        // global.set_2ndCurrident(null);
        // global.set_2ndCurrIcao(null);
        if (!Objects.equals(global.getRecordType(), "S")) {
          global.setRecordType("T");
        }
        global.setGeninhouse("Y");
        // global.set_1stIdent(null);
        // global.set_2ndIdent(null);
        // global.set_2ndIcao(null);
        // global.setNewCycle(null);
        // global.setGenInHouseFlag(null);

        copy(Integer.parseInt(nameIn(this, "global.processingCycle").toString()), "dupl.newCycle");
        setItemProperty("dupl.gen_in_house", ENABLED, PROPERTY_FALSE);
        setItemProperty("dupl.gen_in_house", VISIBLE, PROPERTY_FALSE);

        dupl.setSelectNrecords("Duplicate only the current record and its details.");
        setItemProperty("dupl.replace", VISIBLE, PROPERTY_FALSE);
        setItemProperty("dupl.replace", ENABLED, PROPERTY_FALSE);

        if (Objects.equals(global.getRecordType(), "T")) {
          setItemProperty("dupl.datatyp", ENABLED, PROPERTY_FALSE);
          setItemProperty("dupl.datatyp", VISIBLE, PROPERTY_FALSE);
          setItemProperty("dupl.nrecords_S", ENABLED, PROPERTY_FALSE);
          setItemProperty("dupl.nrecords_S", VISIBLE, PROPERTY_FALSE);

          setItemProperty("dupl.nrecords_T", ENABLED, PROPERTY_TRUE);
          setItemProperty("dupl.nrecords_T", VISIBLE, PROPERTY_TRUE);
        }

        else {
          setItemProperty("dupl.nrecords_T", ENABLED, PROPERTY_FALSE);
          setItemProperty("dupl.nrecords_T", VISIBLE, PROPERTY_FALSE);
        }

        setRadioButtonProperty("repl", "duplDupRep", ENABLED, PROPERTY_TRUE);
        setRadioButtonProperty("repl", "duplDupRep", VISIBLE, PROPERTY_TRUE);

        Integer lnRecCount = 0;
        String fname = HoneyWellUtils.toCamelCase(system.getCurrentForm());

        lnRecCount = app
            .executeFunction(BigDecimal.class, "CPTS", "dup_count_records_fun", "forms_utilities",
                OracleTypes.NUMBER,
                new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
                new ProcedureInParameter("p_where", globals.getGLastQuery(), OracleTypes.VARCHAR))
            .intValue();

        if (lnRecCount > 1 && Objects.equals(global.getRecordType(), "T")
            && !Objects.equals(global.getNewDcrNo(), null)
            && !Objects.equals(global.getNewDcrNo(), "") && !Objects.equals(fname, "gates")) {
          setRadioButtonProperty("ONE", "duplNrecordsT", ENABLED, PROPERTY_FALSE);
          dupl.setNrecordsT(null);
        }

        else {
          setRadioButtonProperty("ONE", "duplNrecordsT", ENABLED, PROPERTY_TRUE);
          dupl.setNrecordsT("ONE");
        }
        if (!OracleHelpers.isNullorEmpty(global.getMasterBlock())) {
          if (Arrays.asList("SID", "STAR", "APPROACH", "HELISID", "HELISTAR", "HELIAPPROACH")
              .contains(global.getMasterBlock().substring(7))) {
            setItemProperty("dupl.ident1st", "promptText", "New Procedure Ident(Optional): ");
            setItemProperty("dupl.ident2nd", "promptText",
                "New Airport/Heliport Ident(Optional): ");
            setItemProperty("dupl.icao2nd", "promptText", "New Airport/Heliport Icao(Optional): ");
          }

          else if (Objects.equals(global.getMasterBlock().substring(7), "ENROUTE_AIRWAY")) {
            setItemProperty("dupl.ident1st", "promptText", "New Route Ident(Optional): ");
          }

          else if (Objects.equals(global.getMasterBlock(), "PL_COMPANY_ROUTE")) {
            setItemProperty("dupl.ident1st", "promptText", "New Company Route Ident(Optional): ");
            setItemProperty("dupl.ident2nd", "promptText", "New Cost Index(Optional): ");
          }

          else if (Objects.equals(global.getMasterBlock(), "PL_TLD_ALTERNATE_DEST")) {
            setItemProperty("dupl.ident2nd", "prompt_text", "New Airport/Fix Ident(Optional): ");
            setItemProperty("dupl.icao2nd", "prompt_text", "New Airport/Fix Icao(Optional): ");
          }
        }

        if (OracleHelpers.isNullorEmpty(global.getFirstCurrident())) {
          setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
          setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
        }

        else {
          setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_TRUE);
          setItemProperty("dupl.ident1st", ENABLED, PROPERTY_TRUE);
        }

        if (OracleHelpers.isNullorEmpty(global.getSecondCurrident())) {
          setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_FALSE);
          setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
          setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
          setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
        } else {
          setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
          setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
          setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_TRUE);
          setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_TRUE);

          if ("PL_COMPANY_ROUTE".equals(global.getMasterBlock())) {
            setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
            setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
          }
        }
        {
          String fName = system.getCurrentForm();
          if ("COMPANY_ROUTE".equals(fName)) {
            setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
            setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);

            setItemProperty("dupl.blnk_Cost_Indx", VISIBLE, PROPERTY_TRUE);
            setItemProperty("dupl.blnk_Cost_Indx", ENABLED, PROPERTY_TRUE);
          } else {
            setItemProperty("dupl.blnk_cost_indx", VISIBLE, PROPERTY_FALSE);
            setItemProperty("dupl.blnk_cost_indx", ENABLED, PROPERTY_FALSE);
          }
        }

        // disableMenuItems();

        {
          String fName = system.getCurrentForm();
          RecordGroup groupId = null;
          String vGroup = "newCustomer";

          if ("GATES".equals(fName)) {
            setItemProperty("dupl.new_Gate_Ident", VISIBLE, PROPERTY_TRUE);
            setItemProperty("dupl.new_Gate_Ident", ENABLED, PROPERTY_TRUE);
          } else {
            setItemProperty("dupl.new_Gate_Ident", VISIBLE, PROPERTY_FALSE);
            setItemProperty("dupl.new_Gate_Ident", ENABLED, PROPERTY_FALSE);
          }

          groupId = findGroup(vGroup);

          if (groupId != null) {
            deleteGroup(groups, "newCustomer");
          } else {
            groupId = createGroup(vGroup);
            addGroupColumn(groupId, "custId", "charColumn", 3);
          }
        }

        {
          String lnGenFlag = null;
//				Integer lnConfirmCopy = 0;
//				String fName = system.getCurrentForm();

          if ("T".equals(global.getRecordType())) {

            lnGenFlag = app.executeFunction(String.class, "CPTS", "Gen_In_House_chk",
                "forms_utilities", OracleTypes.VARCHAR,
                new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
                new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR));

            if ("N".equals(lnGenFlag) && "Y".equals(global.getGenInHouseFlag())) {
              dupl.setGeninouthouse("Y");
              setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
            } else if ("N".equals(lnGenFlag) && "N".equals(global.getGenInHouseFlag())) {
              dupl.setGeninouthouse("N");
              setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
              setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
              setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
            } else {
              dupl.setGeninouthouse("N");
              setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
            }
          } else {
            dupl.setGeninouthouse("N");
            setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
          }
        }
        goBlock("dupl", "newCycle");
        goBlock("custListBlk", "customer");
        OracleHelpers.ResponseMapper(this, resDto);
        log.info(" whenNewFormInstance executed successfully");
        return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
      } catch (Exception e) {
        log.error("Error while Executing the whenNewFormInstance Service");
        OracleHelpers.ResponseMapper(this, resDto);
        return ExceptionUtils.handleException(e, resDto);
      }
    }

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> keyExit(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" keyExit Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
//			OracleHelpers.bulkClassMapper(reqDto, this);
//			if (Objects.equals(global.getStatusWindow(), "MAXIMIZE")) {
//
//				// TODO set_window_property("BASE_WINDOW",window_state,MAXIMIZE);
//
//			}
//
//			else {
//
//				// TODO set_window_property("BASE_WINDOW",window_state,NORMAL);
//
//			}
//
//			// TODO exit_form();
//			OracleHelpers.ResponseMapper(this, resDto);
//			log.info(" keyExit executed successfully");
//			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));

			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
			coreptMenuMmbServiceImpl.toolsDuplicate(this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));

		} catch (Exception e) {
			log.error("Error while Executing the keyExit Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplPreBlock(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplPreBlock Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(message.getRow(system.getCursorRecordIndex()).getDid(), "N")) {
				throw new FormTriggerFailureException();
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplPreBlock executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplPreBlock Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplKeyNxtrec(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplKeyNxtrec Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// null;
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplKeyNxtrec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplKeyNxtrec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplWhenNewBlockInstance(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplWhenNewBlockInstance Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String lnGenFlag = null;
//			Integer lnConfirmCopy = 0;
//			String fname = getApplicationProperty(CALLING_FORM);

			if (Objects.equals(global.getRecordType(), "T")) {

				// lnGenFlag = app.executeFunction(String.class, "CPTS", "Gen_In_House_chk",
				// "forms_utilities",OracleTypes.VARCHAR, global.getCurrentTable(),
				// globals.getGLastQuery());
				lnGenFlag = app.executeFunction(String.class, "CPTS", "Gen_In_House_chk", "forms_utilities",
						OracleTypes.VARCHAR,
						new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR));
				if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "Y")) {
					dupl.setGeninouthouse("Y");
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
				}

				else if (Objects.equals(lnGenFlag, "N") || (Objects.equals(dupl.getNrecordsT(), "ONE")
						&& !Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "N"))) {
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
					setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
					setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
				}

				else {
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
				}
			} else {
				dupl.setGeninouthouse("N");
				setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplWhenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplWhenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplSelectCustomerWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplSelectCustomerWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			replaceCustPrc();
			validateCustomer();
			goBlock("customer", "customerIdent");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplSelectCustomerWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplSelectCustomerWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplNewCycleKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplNewCycleKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("custListBlk.customer");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplNewCycleKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplNewCycleKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplNewCustomerIdentWhenValidateItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplNewCustomerIdentWhenValidateItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			validateCustomer();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplNewCustomerIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplNewCustomerIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	// NEED TO HANDLE IN UI
	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplIdent1stKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplIdent1stKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
//			if (Objects.equals(getItemProperty("dupl.ident2nd", ENABLE), "TRUE")) {
//				goItem("dupl.ident2nd");
//			}
//			else {
//				goItem("dupl.duplicate");
//			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplIdent1stKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplIdent1stKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplIdent2ndKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplIdent2ndKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
//			if (Objects.equals(getItemProperty("ICAO_2ND", enabled), "TRUE")) {
//				goItem("dupl.icao_2nd");
//
//			}
//
//			else {
//				if (Objects.equals(getItemProperty("DUPLICATE", enabled), "TRUE")) {
//					goItem("dupl.duplicate");
//
//				}
//
//				else {
//					goItem("dupl.replace");
//
//				}
//
//			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplIdent2ndKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplIdent2ndKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplIdent2ndWhenValidateItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplIdent2ndWhenValidateItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String fname = HoneyWellUtils.toCamelCase(system.getCurrentForm());
			if (Objects.equals(fname, "companyRoute")) {
			  boolean numCheck = false;
              if(!OracleHelpers.isNullorEmpty(dupl.getIdent2nd()) && dupl.getIdent2nd().matches("\\d+")) {
                numCheck = true;
              }
              
              if(!numCheck) {
                coreptLib.dspMsg("Only 0-9 numbers are allowed for Cost Index");
                throw new FormTriggerFailureException(event);
              }
//				if (Integer
//						.parseInt(nvl(length(translate(ltrim(rtrim(dupl.getIdent2nd())), "0123456789", " ")), 0)) > 0) {
//					coreptLib.dspMsg("Only 0-9 numbers are allowed for Cost Index");
//					throw new FormTriggerFailureException(event);
//				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplIdent2ndWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplIdent2ndWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplBlnkCostIndxWhenCheckboxChanged(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplBlnkCostIndxWhenCheckboxChanged Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(dupl.getBlnkCostIndx(), "Y")) {
				setItemProperty("ident2nd", ENABLED, PROPERTY_FALSE);
				dupl.setIdent2nd(null);
			}

			else {
				setItemProperty("ident2nd", ENABLED, PROPERTY_TRUE);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplBlnkCostIndxWhenCheckboxChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplBlnkCostIndxWhenCheckboxChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplIcao2ndKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplIcao2ndKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("dupl.duplicate");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplIcao2ndKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplIcao2ndKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplDupRepWhenRadioChanged(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplDupRepWhenRadioChanged Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// ln_Xcord NUMBER:= Get_Item_Property('DUPL.DUPLICATE',X_POS);
			// ln_Ycord NUMBER:= Get_Item_Property('DUPL.DUPLICATE',Y_POS);
//			Integer lnAlert = 0;
			String fname = system.getCurrentForm();
			Integer lnRecCount = 0;
			String lnGenFlag = null;

			lnRecCount = app
					.executeFunction(BigDecimal.class, "CPTS", "dup_count_records_fun", "forms_utilities",
							OracleTypes.NUMBER,
							new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR))
					.intValue();

			if (Objects.equals(dupl.getDupRep(), "R")) {
//				setAlertProperty("rep_alert", "ALERT_MESSAGE_TEXT",
//						"you are going to replace (delete + add) the records. please choose the correct processing cycle/dcr numbers");
//				showAlert("rep_alert", true);

				parameter.setRep("Y");
				// if(Objects.equals(getMenuItemProperty(rtrim("Record") + "." +
				// ltrim("Remove"),enabled), "FALSE")) {
				// setMenuItemProperty(rtrim("record") + "." + ltrim("remove"), ENABLED,
				// PROPERTY_TRUE);
				// }

				setItemProperty("dupl.duplicate", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.duplicate", ENABLED, PROPERTY_FALSE);

				setItemProperty("dupl.replace", VISIBLE, PROPERTY_TRUE);
				setItemProperty("dupl.replace", ENABLED, PROPERTY_TRUE);
				setItemProperty("dupl.ident_1st", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.ident_1st", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.ident_2nd", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.ident_2nd", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.icao_2nd", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.icao_2nd", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.newGateIdent", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.newGateIdent", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.geninhouse", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.geninhouse", ENABLED, PROPERTY_FALSE);
				setRadioButtonProperty("STD", "duplDatatyp", VISIBLE, PROPERTY_FALSE);
				setRadioButtonProperty("STD", "duplDatatyp", ENABLED, PROPERTY_FALSE);

				if (Objects.equals(global.getRecordType(), "T")) {
					if (lnRecCount > 1 && !Objects.equals(global.getNewDcrNo(), null)
							&& !Objects.equals(global.getNewDcrNo(), "") && !Objects.equals(fname, "gates")) {
						dupl.setNrecordsT(null);
					} else {
						dupl.setNrecordsT("ONE");
					}

					lnGenFlag = app.executeFunction(String.class, "CPTS", "Gen_In_House_chk", "forms_utilities",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR));

					if (Arrays.asList("airport", "runway", "airportWaypoint", "enrouteWaypoint", "airportNdb",
							"enrouteNdb", "vhf").contains(fname) && !Objects.equals(fname, "gates")) {
						if (Objects.equals(dupl.getNrecordsT(), "ONE")
								&& Objects.equals(global.getGenInHouseFlag(), "N")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
						} else if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "Y")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
						}

						else if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "N")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);

						}

						else {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);

						}

					}

					else {
						if (Objects.equals(global.getGenInHouseFlag(), "N")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);

						}

						else {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
						}
					}
				}

				else {
					dupl.setNrecordsS("ONE");
					dupl.setGeninouthouse("N");
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
				}

				dupl.setSelectNrecords("Replace only the current record and its details.");
				showView("custListCan");
				goItem("dupl.newCycle");

				if (Objects.equals(HoneyWellUtils.toCamelCase(fname), "companyRoute")) {
					setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
					setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
					setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
					setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
				}

				else {
					setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
					setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_FALSE);
				}
			}

			else {
				parameter.setRep("N");
				parameter.setRml("N");

				// if(Objects.equals(getMenuItemProperty(rtrim("Record") + "." +
				// ltrim("Remove"),enabled), "TRUE")) {
				// setMenuItemProperty(rtrim("record") + "." + ltrim("remove"), ENABLED,
				// PROPERTY_FALSE);
				// }

				setItemProperty("dupl.duplicate", VISIBLE, PROPERTY_TRUE);
				setItemProperty("dupl.duplicate", ENABLED, PROPERTY_TRUE);
				setItemProperty("dupl.replace", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.replace", VISIBLE, PROPERTY_FALSE);
				dupl.setSelectNrecords("Duplicate only the current record and its details.");
				goBlock("custListBlk", "");

				// need to check again
				clearBlock("custListCan", null);
				hideView("custListCan");
				if (global.getRecordType().equals("S")) {
					setRadioButtonProperty("STD", "duplDatatyp", VISIBLE, PROPERTY_TRUE);
					setRadioButtonProperty("STD", "duplDatatyp", ENABLED, PROPERTY_TRUE);
				}
				if (Objects.equals(global.getRecordType(), "T")) {
					if (lnRecCount > 1 && !Objects.equals(global.getNewDcrNo(), null)
							&& !Objects.equals(global.getNewDcrNo(), "") && !Objects.equals(fname, "GATES")) {
						dupl.setNrecordsT(null);
					}

					else {
						dupl.setNrecordsT("ONE");
					}
					lnGenFlag = app.executeFunction(String.class, "CPTS", "Gen_In_House_chk", "forms_utilities",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR));

					if (Arrays.asList("airport", "runway", "airportWaypoint", "enrouteWaypoint", "airportNdb",
							"enrouteNdb", "vhf").contains(fname) && !Objects.equals(fname, "GATES")) {
						if (Objects.equals(dupl.getNrecordsT(), "ONE")
								&& Objects.equals(global.getGenInHouseFlag(), "N")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);

						}

						else if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "Y")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
						}

						else if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "N")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
						}

						else {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
						}

					}

					else {
						if (Objects.equals(global.getGenInHouseFlag(), "N")) {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
							setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
						}

						else {
							dupl.setGeninouthouse("N");
							setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
						}
					}
				}

				else {
					dupl.setNrecordsS("ONE");
					dupl.setGeninouthouse("N");
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
				}

				if ((Objects.equals(dupl.getNrecordsT(), "ONE")) || (Objects.equals(dupl.getNrecordsS(), "ONE"))) {
					if (Objects.equals(fname, "GATES")) {
						setItemProperty("dupl.newGateIdent", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.newGateIdent", ENABLED, PROPERTY_TRUE);
					}
					if (!Objects.equals(global.getFirstCurrident(), null)) {
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident1st", ENABLED, PROPERTY_TRUE);
						goItem("dupl.ident_1st");
					}

					if (!Objects.equals(global.getSecondCurrident(), null)) {
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
						setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_TRUE);
						if (Objects.equals(HoneyWellUtils.toCamelCase(global.getMasterBlock()), "plCompanyRoute")) {
							setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
							setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
						}

						if (Objects.equals(HoneyWellUtils.toCamelCase(global.getMasterBlock()), "plTldAlternateDest")) {
							goItem("dupl.ident2nd");
						}
					}
				} else {
					setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
					setItemProperty("dupl.ident2st", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.ident2st", VISIBLE, PROPERTY_FALSE);

					setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
				}
				showView("custListCan");
				goItem("dupl.newCycle");
			}
			if (Objects.equals(dupl.getDupRep(), "R")) {
//				PropertyHelpers.setAlertProperty(event, "rep_alert", "ALERT_MESSAGE_TEXT",
//						"You are going to replace (Delete + Add) the records.\nPlease choose the correct Processing Cycle/DCR\nnumbers",
//						"Replace Records");
				PropertyHelpers.setAlertProperty(event, "rep_alert", "wwarn", "Replace Records",
						"You are going to replace (Delete + Add) the records.\nPlease choose the correct Processing Cycle/DCR\nnumbers",
						"ALERT_MESSAGE_TEXT", null, null, null);
				showAlert("rep_alert", true);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplDupRepWhenRadioChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplDupRepWhenRadioChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplNrecordsTWhenRadioChanged(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplNrecordsTWhenRadioChanged Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String fname = HoneyWellUtils.toCamelCase(system.getCurrentForm());

			if (Objects.equals(fname, "gates") && Objects.equals(dupl.getNrecordsT(), "ONE")) {
				setItemProperty("dupl.newGateIdent", VISIBLE, PROPERTY_TRUE);
				setItemProperty("dupl.newGateIdent", ENABLED, PROPERTY_TRUE);
			}

			else {
				setItemProperty("dupl.newGateIdent", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.newGateIdent", ENABLED, PROPERTY_FALSE);
			}

			Integer lnRecCount = 0;
			Integer lnConfirmCopy = 0;
			String lnGenFlag = null;

			if (Arrays.asList("SET").contains(dupl.getNrecordsT())) {

				lnRecCount = app
						.executeFunction(BigDecimal.class, "CPTS", "dup_count_records_fun", "forms_utilities",
								OracleTypes.NUMBER,
								new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR))
						.intValue();

				alertDetails.getCurrent();
				if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
					displayAlert.moreButtonsStop("S", "Copy Records Confirmation", "Total Number of Records to be copied are "
							+ lnRecCount + ". Do you\nwish to copy the records, Please confirm.", "Continue", "Cancel",
							"");
					OracleHelpers.bulkClassMapper(displayAlert, this);
					alertDetails.createNewRecord("copyRecords1");
					throw new AlertException(event, alertDetails);
				} else {
					lnConfirmCopy = alertDetails.getAlertValue("copyRecords1", alertDetails.getCurrentAlert());
				}
				if (Objects.equals(lnConfirmCopy, 1)) {
					if (Objects.equals(fname, "companyRoute")) {
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
					}

					else {
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_FALSE);
						dupl.setIdent1st(null);
						dupl.setIdent2nd(null);
						dupl.setIcao2nd(null);
						setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
					}
					setItemProperty("dupl.geninhouse", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.geninhouse", VISIBLE, PROPERTY_FALSE);
					if (Objects.equals(dupl.getDupRep(), "D")) {
						dupl.setSelectNrecords(
								"Duplicate the set of queried records and their details.\n- Only records match the last query are going to be copied. Any new-created/updated\ndata, which don't match the last query will not be copied.");

					}

					else {
						dupl.setSelectNrecords("Replace the set of queried records and their details." + chr(10)
								+ " - Only records match the last query are going to be copied. Any new-created/updated "
								+ chr(10) + "    data, which don't match the last query will not be copied.");

					}

					lnGenFlag = app.executeFunction(String.class, "CPTS", "Gen_In_House_chk", "forms_utilities",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR));

					if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "Y")) {
						dupl.setGeninouthouse("N");
						setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
					}

					else if (Objects.equals(lnGenFlag, "N") && Objects.equals(global.getGenInHouseFlag(), "N")) {
						dupl.setGeninouthouse("N");
						setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
						setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
						setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
					}

					else {
						dupl.setGeninouthouse("N");
						setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
					}
				} else {
					dupl.setNrecordsT("ONE");
				}
			}

			else {
				if (Objects.equals(dupl.getDupRep(), "D")) {
					dupl.setSelectNrecords("Duplicate only the current record and its details.");
					setItemProperty("dupl.geninhouse", ENABLED, PROPERTY_FALSE);
					setItemProperty("dupl.geninhouse", VISIBLE, PROPERTY_FALSE);

					if (!Objects.equals(global.getFirstCurrident(), null)) {
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident1st", ENABLED, PROPERTY_TRUE);
						goItem("ident1st");
					}
					if (!Objects.equals(global.getSecondCurrident(), null)) {
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
						setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_TRUE);

						if (Objects.equals(global.getMasterBlock(), "PL_COMPANY_ROUTE")) {
							setItemProperty("dupl.icao_2nd", ENABLED, PROPERTY_FALSE);
							setItemProperty("dupl.icao_2nd", VISIBLE, PROPERTY_FALSE);
						}

						if (Objects.equals(global.getMasterBlock(), "PL_TLD_ALTERNATE_DEST")) {
							goItem("dupl.ident2nd");
						}
					}
				} else {
					dupl.setSelectNrecords("Replace only the current record and its details.");
				}
				if (Objects.equals(global.getGenInHouseFlag(), "N")) {
					dupl.setGeninouthouse("N");
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_TRUE);
					setItemProperty("dupl.geninouthouse", INSERT_ALLOWED, PROPERTY_TRUE);
					setItemProperty("dupl.geninouthouse", UPDATE_ALLOWED, PROPERTY_TRUE);
				} else {
					dupl.setGeninouthouse("N");
					setItemProperty("dupl.geninouthouse", ENABLED, PROPERTY_FALSE);
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplNrecordsTWhenRadioChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplNrecordsTWhenRadioChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplNrecordsSWhenRadioChanged(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplNrecordsSWhenRadioChanged Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapper(reqDto, this);
			String fname = system.getCurrentForm();

			if (Objects.equals(fname, "GATES") && Objects.equals(dupl.getNrecordsS(), "ONE")) {
				setItemProperty("dupl.newGateIdent", VISIBLE, PROPERTY_TRUE);
				setItemProperty("dupl.newGateIdent", ENABLED, PROPERTY_TRUE);
			}

			else {
				setItemProperty("dupl.newGateIdent", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.newGateIdent", ENABLED, PROPERTY_FALSE);
			}

			Integer lnRecCount = 0;
			Integer lnConfirmCopy = 0;
			// String lnGenFlag = null;

			if (Objects.equals(dupl.getNrecordsS(), "SET")) {

				lnRecCount = app
						.executeFunction(BigDecimal.class, "CPTS", "dup_count_records_fun", "forms_utilities",
								OracleTypes.NUMBER,
								new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR))
						.intValue();

				alertDetails.getCurrent();
				if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
				  displayAlert.moreButtonsStop("S", "Copy Records Confirmation", "Total Number of Records to be copied are "
							+ lnRecCount + ". Do you\nwish to copy the records, Please confirm.", "Continue", "Cancel",
							"");
					OracleHelpers.bulkClassMapper(displayAlert, this);
					alertDetails.createNewRecord("copyRecords1");
					throw new AlertException(event, alertDetails);
				} else {
					lnConfirmCopy = alertDetails.getAlertValue("copyRecords1", alertDetails.getCurrentAlert());
				}

				if (Objects.equals(lnConfirmCopy, 1)) {
					if (Objects.equals(fname, "companyRoute")) {
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
					}

					else {
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
						dupl.setIdent1st(null);
						dupl.setIdent2nd(null);
						dupl.setIcao2nd(null);
						setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_FALSE);
						setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);

					}
					if (Objects.equals(dupl.getDupRep(), "D")) {
						dupl.setSelectNrecords("Duplicate the set of queried records and their details." + chr(10)
								+ " - Only records match the last query are going to be copied. Any new-created/updated "
								+ chr(10) + "    data, which don't match the last query will not be copied.");
					}

					else {
						dupl.setSelectNrecords("Replace the set of queried records and their details." + chr(10)
								+ " - Only records match the last query are going to be copied. Any new-created/updated "
								+ chr(10) + "    data, which don't match the last query will not be copied.");
					}
				}

				else {
					dupl.setNrecordsS("ONE");
				}
			}

			else {
				if (Objects.equals(dupl.getDupRep(), "D")) {
					dupl.setSelectNrecords("Duplicate only the current record and its details.");
					if (!Objects.equals(global.getFirstCurrident(), null)) {
						setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_TRUE);
						setItemProperty("dupl.ident1st", ENABLED, PROPERTY_TRUE);
						goItem("ident1st");
					}
					if (!OracleHelpers.isNullorEmpty(global.getSecondCurrident())) {
						if (Objects.equals(global.getMasterBlock(), "PL_COMPANY_ROUTE")) {
							setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
							setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_FALSE);
							setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_FALSE);
						}

						else {
							setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
							setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
							setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_TRUE);
							setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_TRUE);
//						setItemProperty("duplIdent1st", VISIBLE, PROPERTY_TRUE);
//						setItemProperty("duplIdent2nd", VISIBLE, PROPERTY_TRUE);
//						setItemProperty("duplIcao2nd", VISIBLE, PROPERTY_TRUE);
						}

						if (Objects.equals(global.getMasterBlock(), "PL_TLD_ALTERNATE_DEST")) {
							goItem("ident2nd");
						}
					}
				}
				dupl.setSelectNrecords("Replace only the current record and its details.");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplNrecordsSWhenRadioChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplNrecordsSWhenRadioChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplDatatypWhenRadioChanged(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplDatatypWhenRadioChanged Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(dupl.getDatatyp(), "STD")) {
				setItemProperty("dupl.select_Customer", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.select_Customer", ENABLED, PROPERTY_FALSE);
				hideView("custListCan");
				dupl.setNewCycle(null);
			}

			else {
				dupl.setNewCycle(Integer.parseInt(global.getProcessingCycle()));
				setItemProperty("dupl.select_Customer", VISIBLE, PROPERTY_TRUE);
				setItemProperty("dupl.select_Customer", ENABLED, PROPERTY_TRUE);
				showView("custListCan");
				goItem("dupl.new_cycle");

			}
			if (Objects.equals(dupl.getNrecordsS(), "ONE")) {
				if (!Objects.equals(global.getFirstCurrident(), null)) {
					setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_TRUE);
					setItemProperty("dupl.ident1st", ENABLED, PROPERTY_TRUE);
					if (Objects.equals(dupl.getDatatyp(), "STD")) {
						goItem("dupl.new_Cycle");
					}

					else {
						goItem("dupl.ident1st");
					}
				}

				if (!Objects.equals(global.getSecondCurrident(), null)) {
					setItemProperty("dupl.ident2nd", VISIBLE, PROPERTY_TRUE);
					setItemProperty("dupl.ident2nd", ENABLED, PROPERTY_TRUE);
					setItemProperty("dupl.icao2nd", VISIBLE, PROPERTY_TRUE);
					setItemProperty("dupl.icao2nd", ENABLED, PROPERTY_TRUE);
					if (Objects.equals(global.getMasterBlock(), "plTldAlternateDest")) {
						goItem("dupl.ident2nd");
					}
				}
			} else if ("SET".equals(dupl.getNrecordsS())) {
				dupl.setIdent1st(null);
				dupl.setIdent2nd(null);
				dupl.setIcao2nd(null);

				setItemProperty("dupl.ident1st", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.ident1st", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.ident2st", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.ident2st", VISIBLE, PROPERTY_FALSE);
				setItemProperty("dupl.icao2st", ENABLED, PROPERTY_FALSE);
				setItemProperty("dupl.icao2st", VISIBLE, PROPERTY_FALSE);
				goItem("dupl.new_cycle");
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplDatatypWhenRadioChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplDatatypWhenRadioChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplDuplicateWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplDuplicateWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String query = null;
			Record rec = null;
			String fname = HoneyWellUtils.toCamelCase(system.getCurrentForm());
			//
			if (Objects.equals(dupl.getNrecordsT(), null)) {
				coreptLib.dspMsg("Please select the Copy Displayed Records option to\ncopy/replace records");
				throw new FormTriggerFailureException();
			}

			if (Objects.equals(fname, "companyRoute")) {

              if (!OracleHelpers.isNullorEmpty(dupl.getIdent2nd())) {
//					if (dupl.getIdent2nd().length() > 10) {
                boolean numCheck = false;
                if (!OracleHelpers.isNullorEmpty(dupl.getIdent2nd())
                    && dupl.getIdent2nd().matches("\\d+")) {
                  numCheck = true;
                }

                if (!numCheck) {
                  coreptLib.dspMsg("Only 0-9 numbers are allowed for Cost Index");
                  throw new FormTriggerFailureException(event);
                }

//				if (Integer.parseInt(
//						nvl(length(translate(ltrim(rtrim(dupl.getIdent2nd())), " 0123456789", " ")), 0)) > 0) {
//						coreptLib.dspMsg("Only 0-9 numbers are allowed for Cost Index");
//						throw new FormTriggerFailureException(event);
//					}
              }
			}

			if (Objects.equals(message.getRow(0).getDid(), "N")) {
				throw new FormTriggerFailureException();
			}

			Integer lnButton = 0;

			alertDetails.getCurrent();
			if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
			  displayAlert.moreButtonsStop("S", "Copy Record",
						"You are going to Copy the record(s). Select an option to Proceed:\n( Note:Copy and Refresh may take more time )"
								+ chr(10),
						"Copy Only", "Copy and Refresh", "Cancel");
				OracleHelpers.bulkClassMapper(displayAlert, this);
				alertDetails.createNewRecord("copyRecord1");
				throw new AlertException(event, alertDetails);
			} else {
				lnButton = alertDetails.getAlertValue("copyRecord1", alertDetails.getCurrentAlert());
			}

			if (Objects.equals(lnButton, 1)) {
				parameter.setRml("R");
			}

			else if (Objects.equals(lnButton, 2)) {
				parameter.setRml("Y");
			}

			else {
				parameter.setRml("N");
				throw new FormTriggerFailureException(event);
			}

			if (!Objects.equals(dupl.getDatatyp(), "STD")) {

				checkBlankDcrPrc();

				replaceCustPrc();

				if (Objects.equals(rtrim(parameter.getRepCust()), null)) {
					goItem("custListBlk.customer");
					coreptLib.dspMsg("Please select Customer Ident(s) that you want to copy records for.");
					throw new FormTriggerFailureException(event);
				}

				else {
					validateCustomer();
					String vsDataSupplName = null;
					Integer vnCheck = 0;

					if (!Objects.equals(parameter.getCustIdents(), null)) {

						query = """
								SELECT data_supplier_name
								                 from data_supplier
								                WHERE data_supplier = ?
								""";
						rec = app.selectInto(query, global.getDataSupplier());
						vsDataSupplName = rec.getString();

						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						  displayAlert.moreButtonsStop("S", "Different Supplier",
									"The customer(s) " + parameter.getCustIdents()
											+ " is/are not associated withlogon\nData Supplier " + vsDataSupplName
											+ "; Do you wish to copy to the\nselected customer(s),Please confirm.",
									"Continue", "Cancel", "");
							OracleHelpers.bulkClassMapper(displayAlert, this);
							alertDetails.createNewRecord("copyRecord1");
							throw new AlertException(event, alertDetails);
						} else {
							lnButton = alertDetails.getAlertValue("copyRecord1", alertDetails.getCurrentAlert());

							// Coverity-Fixes
							log.info(toString(lnButton));
						}

						if (Objects.equals(vnCheck, 2)) {
							throw new FormTriggerFailureException();
						}
					}
				}
			}

			if (Objects.equals(dupl.getNrecordsT(), "SET") || Objects.equals(dupl.getNrecordsS(), "SET")) {
				if (Objects.equals(global.getGLastQuery(), null)) {

					coreptLib.dspMsg("Please give a query for the set of records you are going to copy.");
					throw new FormTriggerFailureException(event);

				}
				Integer values = app
						.executeFunction(BigDecimal.class, "CPTS", "count_records", "forms_utilities",
								OracleTypes.NUMBER,
								new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_field", "PROCESSING_CYCLE", OracleTypes.VARCHAR))
						.intValue();
				if (values > 1) {
					coreptLib.dspMsg(
							"Can not copy for a set of records that is not\nrestricted by one Processing Cycle.");
					throw new FormTriggerFailureException(event);
				}

				else if (Objects.equals(instr(global.getGLastQuery(), "PROCESSING_CYCLE="), 0)) {
					global.setGLastQuery(global.getGLastQuery() + " and processing_cycle= " + global.getRecordCycle());
				}
			}

			if (Objects.equals(dupl.getNrecordsT(), "SET")) {
				Integer values = app.executeFunction(BigDecimal.class, "CPTS", "count_records", "forms_utilities",
						OracleTypes.NUMBER,
						new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_field", "CUSTOMER_IDENT", OracleTypes.VARCHAR)).intValue();

				if (values > 1) {
					coreptLib.dspMsg(
							"Can not copy for a set of tailored records that is not\nrestricted by one Customer Ident.");
					throw new FormTriggerFailureException(event);
				}

				else if (Objects.equals(instr(global.getGLastQuery(), "CUSTOMER_IDENT"), 0)) {
					global.setGLastQuery(
							global.getGLastQuery() + " and customer_ident = '" + global.getCurrCustomerIdent() + "'");
				}
			}

			if ((Objects.equals(dupl.getNrecordsS(), "SET") && Objects.equals(dupl.getDatatyp(), "STD")
					&& Objects.equals(dupl.getNewCycle(), global.getRecordCycle()))) {
				coreptLib.dspMsg("Can not copy this set of records in the same processing cycle.");
				throw new FormTriggerFailureException(event);
			}
			if (Objects.equals(rtrim(toString(dupl.getNewCycle())), null)) {
				goItem("newCycle");
				coreptLib.dspMsg("Please provide the CYCLE that you want to copy records for.");
				throw new FormTriggerFailureException(event);
			}

			else {
//				Integer vExist = 0;
				try {
					query = """
							SELECT 1 from cycle WHERE cycle = ?
							""";
					rec = app.selectInto(query, dupl.getNewCycle());
//					vExist = rec.getInt();

					// coverity-fixes
					rec.getInt();
				} catch (NoDataFoundException e) {
					coreptLib.dspMsg("Please assign a valid Processing Cycle.");
					goItem("newCycle");
					throw new FormTriggerFailureException(event);
				}
			}
			// coverity-fixes
			if (Objects.equals(HoneyWellUtils.toCamelCase( substr(global.getMasterBlock(), 8)), "enrouteAirway")) {

				if (length(dupl.getIdent1st()) > 5) {
					coreptLib.dspMsg("The length of a Route Ident can not greater than 5. ");
					goItem("ident1st");
					throw new FormTriggerFailureException(event);
				}
			}

			else if (Arrays.asList("plCompanyRoute", "plTldAlternateDest")
					.contains(HoneyWellUtils.toCamelCase(global.getMasterBlock()))) {
				// null;
			}

			else {

				if (length(dupl.getIdent1st()) > 6) {
					coreptLib.dspMsg("The length of a Procedure Ident can not greater than 6. ");
					goItem("ident1st");
					throw new FormTriggerFailureException(event);
				}

				if (length(dupl.getIdent2nd()) > 4) {
					coreptLib.dspMsg("The length of an Airport/Heliport Ident can not greater than 4. ");
					goItem("ident2nd");
					throw new FormTriggerFailureException(event);
				}
			}

			checkParameters();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplDuplicateWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplDuplicateWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplDuplicateKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplDuplicateKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("dupl.cancel");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplDuplicateKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplDuplicateKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplReplaceWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplReplaceWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String fname = HoneyWellUtils.toCamelCase(system.getCurrentForm());
			String query = null;
			Record rec = null;
			// custListBlk.remove(custListBlk.size() -1);
			if (Objects.equals(dupl.getNrecordsT(), null)) {
				coreptLib.dspMsg("Please select the Copy Displayed Records option to\ncopy/replace records");
				throw new FormTriggerFailureException(event);
			}

			if (Objects.equals(fname, "companyRoute")) {
				if (Integer.parseInt(
						nvl(length(translate(ltrim(rtrim(dupl.getIdent2nd())), " 0123456789", " ")), 0)) > 0) {
					coreptLib.dspMsg("Only 0-9 numbers are allowed for Cost Index");
					throw new FormTriggerFailureException(event);
				}
			}

			if (!Objects.equals(custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(), null)) {
				if (Objects.equals(global.getCurrCustomerIdent(),
						custListBlk.getRow(system.getCursorRecordIndex()).getCustomer())
						&& (global.getProcessingCycle().equals(toString(dupl.getNewCycle())))) {
					coreptLib.dspMsg(
							"Entered customer for Copy/Replace option should not be\nsame as Copy from customer '"
									+ global.getCurrCustomerIdent() + "' and from Cycle\n" + global.getProcessingCycle()
									+ ". ");
					throw new FormTriggerFailureException(event);
				}
			}

			if (Objects.equals(message.getRow(system.getCursorRecordIndex()).getDid(), "N")) {
				throw new FormTriggerFailureException();
			}

			checkBlankDcrPrc();
			Integer lnButton = 0;

			alertDetails.getCurrent();
			if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
			  displayAlert.moreButtonsStop("S", "Replace Record",
						"You are going to Replace the record(s). Select an option to Proceed: \n"
								+ "( Note:Replace and Refresh may take more time )\n",
						"Replace Only", "Replace and Refresh", "Cancel");
				OracleHelpers.bulkClassMapper(displayAlert, this);
				alertDetails.createNewRecord("replaceRecord1");
				throw new AlertException(event, alertDetails);
			} else {
				lnButton = alertDetails.getAlertValue("replaceRecord1", alertDetails.getCurrentAlert());
			}

			if (Objects.equals(lnButton, 1)) {
				parameter.setRml("R");
			}

			else if (Objects.equals(lnButton, 2)) {
				parameter.setRml("Y");
			}

			else {
				parameter.setRml("N");
				throw new FormTriggerFailureException();
			}

			if (!Objects.equals(dupl.getDatatyp(), "std")) {
				replaceCustPrc();
				if (Objects.equals(rtrim(parameter.getRepCust()), null)) {
					goItem("custListBlk.customer");
					coreptLib.dspMsg("Please select Customer Ident(s) that you want to copy records for.");
					throw new FormTriggerFailureException(event);
				}

				else {
					validateCustomer();
					String vsDataSupplName = null;
					Integer vnCheck = 0;

					if (!Objects.equals(parameter.getCustIdents(), null)) {

						query = """
								SELECT data_supplier_name
								                 from data_supplier
								                WHERE data_supplier = ?
								""";
						rec = app.selectInto(query, global.getDataSupplier());
						vsDataSupplName = rec.getString();

						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						  displayAlert.moreButtonsStop("S", "Different Supplier",
									"The customer(s) " + parameter.getCustIdents()
											+ " is/are not associated with  logon\nData Supplier " + vsDataSupplName
											+ "; Do you wish to copy to the\nselected customer(s),\nPlease confirm.",
									"Continue", "Cancel", "");
							OracleHelpers.bulkClassMapper(displayAlert, this);
							alertDetails.createNewRecord("differentSupplier1");
							throw new AlertException(event, alertDetails);
						} else {
							vnCheck = alertDetails.getAlertValue("differentSupplier1", alertDetails.getCurrentAlert());
						}
						if (Objects.equals(vnCheck, 2)) {
							throw new FormTriggerFailureException();
						}
					}
				}
			}

			if (Objects.equals(dupl.getNrecordsT(), "SET") || Objects.equals(dupl.getNrecordsS(), "SET")) {

				if (Objects.equals(global.getGLastQuery(), null)) {
					coreptLib.dspMsg("Please give a query for the set of records you are going to copy.");
					throw new FormTriggerFailureException(event);
				}

				Integer values = app
						.executeFunction(BigDecimal.class, "CPTS", "count_records", "forms_utilities",
								OracleTypes.NUMBER,
								new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_field", "PROCESSING_CYCLE", OracleTypes.VARCHAR))
						.intValue();
				if (values > 1) {
					coreptLib.dspMsg(
							"Can not copy for a set of records that is not\nrestricted by one Processing Cycle.");
					throw new FormTriggerFailureException(event);
				}

				else if (Objects.equals(instr(global.getGLastQuery(), "PROCESSING_CYCLE="), 0)) {
					global.setGLastQuery(global.getGLastQuery() + " and processing_cycle= " + global.getRecordCycle());
				}
			}

			if (Objects.equals(dupl.getNrecordsT(), "SET")) {

				Integer values = app.executeFunction(BigDecimal.class, "CPTS", "count_records", "forms_utilities",
						OracleTypes.NUMBER,
						new ProcedureInParameter("p_table", global.getCurrentTable(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_where", global.getGLastQuery(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_field", "CUSTOMER_IDENT", OracleTypes.VARCHAR)).intValue();
				if (values > 1) {
					coreptLib.dspMsg(
							"Can not copy for a set of tailored records that is not\nrestricted by one Customer Ident.");
					throw new FormTriggerFailureException(event);
				}

				else if (Objects.equals(instr(global.getGLastQuery(), "CUSTOMER_IDENT"), 0)) {
					global.setGLastQuery(
							global.getGLastQuery() + " and customer_ident = '" + global.getCurrCustomerIdent() + "'");
				}

			}

			if ((Objects.equals(dupl.getNrecordsS(), "SET") && Objects.equals(dupl.getDatatyp(), "STD")
					&& Objects.equals(dupl.getNewCycle(), global.getRecordCycle()))) {

				coreptLib.dspMsg("Can not copy this set of records in the same processing cycle.");
				throw new FormTriggerFailureException(event);

			}

			if (Objects.equals(rtrim(dupl.getNewCycle().toString()), null)) {
				goItem("newCycle");
				coreptLib.dspMsg("Please provide the CYCLE that you want to copy records for.");
				throw new FormTriggerFailureException(event);
			}

			else {
//				Integer vExist = 0;
				try {
					query = """
							SELECT 1
							           from cycle
							          WHERE cycle = ?
							""";
					rec = app.selectInto(query, dupl.getNewCycle());
//					vExist = rec.getInt();

					// coverity-fixes
					rec.getInt();
				} catch (NoDataFoundException e) {
					coreptLib.dspMsg("Please assign a valid Processing Cycle.");
					goItem("newCycle");
					throw new FormTriggerFailureException(event);
				}
			}
			// Coverity-fixes
			if (Objects.equals(substr(global.getMasterBlock(), 0), "ENROUTE_AIRWAY")) {
				if (length(dupl.getIdent1st()) > 5) {

					coreptLib.dspMsg("The length of a Route Ident can not greater than 5. ");
					goItem("ident1st");
					throw new FormTriggerFailureException(event);

				}
			}

			else if (Arrays.asList("PL_COMPANY_ROUTE", "PL_TLD_ALTERNATE_DEST").contains(global.getMasterBlock())) {
				// null;
			} else {
				if (length(dupl.getIdent1st()) > 6) {

					coreptLib.dspMsg("The length of a Procedure Ident can not greater than 6. ");
					goItem("ident1st");
					throw new FormTriggerFailureException(event);

				}

				if (length(dupl.getIdent2nd()) > 4) {
					coreptLib.dspMsg("The length of an Airport/Heliport Ident can not greater than 4. ");
					goItem("ident2nd");
					throw new FormTriggerFailureException(event);
				}
			}
			checkParameters();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplReplaceWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplReplaceWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplReplaceKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplReplaceKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("dupl.cancel");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplReplaceKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplReplaceKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplNewGateIdentKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplNewGateIdentKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("dupl.duplicate");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplNewGateIdentKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplNewGateIdentKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplNewReplaceCustWhenValidateItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplNewReplaceCustWhenValidateItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// null;
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplNewReplaceCustWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplNewReplaceCustWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplCancelWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplCancelWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			global.setNewCustomerIdent(null);
			global.setNumToDuplicate(null);
			global.setDoDuplicate("N");
			global.setFirstIdent(null);
			global.setSecondIdent(null);
			global.setSecondIcao(null);
			// Exit_Form(NO_VALIDATE); -- SCR 7842 - Shrikant K - 29-may-2018
//			exitForm();
			// pc3_do_key('exit_form');
//			pitssCon30.pc3dokey("exitForm");

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplCancelWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplCancelWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> duplCancelKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" duplCancelKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("dupl.dup_rep");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" duplCancelKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the duplCancelKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerWhenNewBlockInstance(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerWhenNewBlockInstance Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			message.getRow(0).setDid("N");
//			pitssCon30.pc3dokey("executeQuery");   temp fix need to discuss.
			executeQuery(this, lower(system.getCursorBlock()), null, null, null);
			// executeQuery(this,"customer","create_dcr_number = 1111111",null,null);

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerWhenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerWhenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerKeyExeqry(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerKeyExeqry Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// need to remove whereclause
			// executeQuery(this,"customer","create_dcr_number = 1111111",null,null);
			executeQuery(this, "customer", null, null, null);
			goBlock("customer", "customerIdent");
			// firstRecord("customer");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerKeyExeqry executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerKeyExeqry Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerPostQuery(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerPostQuery Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			RecordGroup groupId = null;
			Integer vRow = 0;
			RecordGroupColumn colId = null;
			String colVal = null;

			groupId = findGroup("NEW_CUSTOMER");
			if (idNull(groupId)) {
				// null;
			}

			else {
				colId = findColumn("NEW_CUSTOMER.CUST_ID");
				if (idNull(colId)) {
					coreptLib.dspMsg("wrong!");
					throw new FormTriggerFailureException(event);
				}

				// coverity-fixes
				else if (colId != null) {
					vRow = getGroupRowCount(groupId);
					for (int i = 0; i < vRow; i++) {
						colVal = getGroupCharCell("newCustomer." + colId.getName(), i);
						for (Integer index = 0; index < customer.size(); index++) {
							if (Objects.equals(colVal, customer.getRow(index).getCustomerIdent())) {
								customer.getRow(index).setChk("Y");
								break;
							}
						}
					}
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerPostQuery executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerChkWhenCheckboxChanged(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerChkWhenCheckboxChanged Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vGroup = "newCustomer";
			RecordGroup groupId = findGroup(vGroup);
			Integer vRow = getGroupRowCount(groupId);
			RecordGroupColumn colId = findColumn("newCustomer.custId");
			String colVal = "";
			List<String> customerList = new ArrayList<>();

//			if(custListBlk.size()==1 && custListBlk.getRow(0).getRecordStatus().equals("NEW") )
//				custListBlk.removeAt(0);

			if (Objects.equals(customer.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
				addGroupRow(groupId, "endOfGroup");
				// vRow = vRow + 1;
				setGroupCharCell(groupId, "custId", vRow,
						customer.getRow(system.getCursorRecordIndex()).getCustomerIdent());
				goBlock("custListBlk", "");
				firstRecord("custListBlk");
				for (Integer index = groupId.getRowCount() - 1; index < groupId.getRowCount(); index++) {
					if (!Objects.equals(custListBlk.getRow(index).getCustomer(),
							customer.getRow(system.getCursorRecordIndex()).getCustomerIdent())
							|| Objects.equals(custListBlk.getRow(index).getCustomer(), null)
							|| custListBlk.getRow(index).getCustomer().equals("")) {
						customerList.add(customer.getRow(system.getCursorRecordIndex()).getCustomerIdent());
						custListBlk.add(new CustListBlk());
						custListBlk.getRow(index).setCustomer(customerList.get(0));
						// custListBlk.add(new CustListBlk());
//						custListBlk.getRow(index)
//								.setCustomer(customer.getRow(system.getCursorRecordIndex()).getCustomerIdent());
						break;
					}

					nextRecord("custListBlk");
//					} else {
//						CustListBlk cutListBlkNew = new CustListBlk();
//						cutListBlkNew.setCustomer(customer.getRow(system.getCursorRecordIndex()).getCustomerIdent());
//						custListBlk.add(cutListBlkNew);
//						break;
//					}
				}
				for (int j = 0; j <= custListBlk.size() - 1; j++) {
					if (Objects.equals(custListBlk.getRow(j).getCustomer(), null)) {
						custListBlk.remove(j);
					}
				}
//				String custlisval = customerList.get(0);
//				for (int i = groupId.getRowCount(); i <= groupId.getRowCount(); i++) {
//					custListBlk.getRow(i).setCustomer(custlisval);
//				}
			}

			else if (Objects.equals(nvl(customer.getRow(system.getCursorRecordIndex()).getChk(), "N"), "N")) {
				for (int i = 0; i < vRow; i++) {
					colVal = getGroupCharCell("newCustomer." + colId.getName(), i);
					if (Objects.equals(colVal, customer.getRow(system.getCursorRecordIndex()).getCustomerIdent())) {
						deleteGroupRow(groupId.getName(), i);
						custListBlk.remove(i);
						break;
					}
				}
				goBlock("custListBlk", "");
				firstRecord("custListBlk");

				for (Integer index = 0; index < custListBlk.size(); index++) {
					if (Objects.equals(custListBlk.getRow(index).getCustomer(),
							customer.getRow(system.getCursorRecordIndex()).getCustomerIdent())) {
						custListBlk.remove(index);
						// custListBlk.removeAt(index); //------Need to test

						// List<CustListBlk> custListBlk1 = custListBlk.getData();
						// custListBlk1.remove(index);
						// custListBlk.setData(custListBlk1);
						// deleteRecord("");
						break;
					}
					// nextRecord("");
				}
				if (custListBlk.size() == 0) {
					CustListBlk custList = new CustListBlk();
					custList.setRecordStatus("NEW");
					custListBlk.add(custList);
				}
			}
			if (!Objects.equals(custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(), null)
					&& Objects.equals(dupl.getDupRep(), "R")) {

				if (Objects.equals(global.getCurrCustomerIdent(),
						custListBlk.getRow(system.getCursorRecordIndex()).getCustomer())
						&& (global.getProcessingCycle().equals(toString(dupl.getNewCycle())))) {

					coreptLib.dspMsg(
							"Entered customer for Copy/Replace option should not be\nsame as Copy from customer '"
									+ global.getCurrCustomerIdent() + "' and from Cycle\n" + global.getProcessingCycle()
									+ ". ");
					throw new FormTriggerFailureException(event);
				}
			}
			goBlock("customer", "");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerChkWhenCheckboxChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerChkWhenCheckboxChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerClearWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerClearWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			RecordGroup groupId = findGroup("NEW_CUSTOMER");
			Integer vRow = getGroupRowCount(groupId);
			String colVal = null;

			for (Integer index = 0; index < customer.size(); index++) {
				if (Objects.equals(customer.getRow(index).getChk(), "Y")) {
					customer.getRow(index).setChk("N");
					for (int i = 0; i < vRow; i++) {
						colVal = getGroupCharCell("newCustomer.custId", i);
						if (Objects.equals(colVal, customer.getRow(index).getCustomerIdent())) {
							deleteGroupRow("newCustomer", i);
							break;
						}
					}
					goBlock("custListBlk", "customer");
					custListBlk.remove(0);
					goBlock("customer", "customerIdent");
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerClearWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerClearWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerCheckAllWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerCheckAllWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			RecordGroup groupId = findGroup("newCustomer");
			Integer vRow = getGroupRowCount(groupId);
			
			if( custListBlk.getData().size()==1 && Objects.equals(custListBlk.getData().get(0).getCustomer(), "")) {
				custListBlk.getData().clear();
			}
			
			for (Integer index = 0; index < customer.size(); index++) {
				if (Objects.equals(nvl(customer.getRow(index).getChk(), "N"), "N")) {
					customer.getRow(index).setChk("Y");
					addGroupRow(groupId, "endOfGroup");
					setGroupCharCell(groupId, "custId", vRow, customer.getRow(index).getCustomerIdent());
					CustListBlk blk = new CustListBlk();
					blk.setCustomer(customer.getRow(index).getCustomerIdent());
					custListBlk.getData().add(blk);
					nextRecord("custListBlk");
					vRow++;
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerCheckAllWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerCheckAllWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> customerCloseWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" customerCloseWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			message.getRow(0).setDid("Y");
			hideView("customer");
			goBlock("custListBlk", "customer");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" customerCloseWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the customerCloseWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> custListBlkCustomerWhenValidateItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" custListBlkCustomerWhenValidateItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (!Objects.equals(custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(), null)
					&& Objects.equals(dupl.getDupRep(), "R")) {

				if (Objects.equals(global.getCurrCustomerIdent(),
						custListBlk.getRow(system.getCursorRecordIndex()).getCustomer())
						&& (global.getProcessingCycle().equals(toString(dupl.getNewCycle())))) {

					coreptLib.dspMsg(
							"Entered customer for Copy/Replace option should not be\nsame as Copy from customer '"
									+ global.getCurrCustomerIdent() + "' and from Cycle\n" + global.getProcessingCycle()
									+ ". ");
					throw new FormTriggerFailureException(event);
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" custListBlkCustomerWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the custListBlkCustomerWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> custListBlkDcrNumberWhenNewItemInstance(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" custListBlkDcrNumberWhenNewItemInstance Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
//			Boolean lsReturn = null;

			// coverity-fixes
			Boolean lsReturn = true;
			Integer lnDcr = 0;
			String lsInstanceType = null;
			Integer lnOldCycle = null;
			Integer lnMaxCycle = null;
			Integer lnMaxCycle1 = null;
			Integer lnMaxCycle2 = null;
			Integer lnNextCycle = 0;
			Integer lnProcCycle = 0;
			Integer lnCntDcr = 0;
			Integer lnDcrNo = 0;
			Integer vSelect = 0;

			String query = null;
			Record rec = null;

			try {
				custListBlk.getRow(system.getCursorRecordIndex()).setDcrNumber(null);

				try {

					query = """
							SELECT UPPER(Instance_Type) from cptcontrol.system_software_identification
							""";
					rec = app.selectInto(query);
					lsInstanceType = rec.getString();
				}
				// OTHERS
				catch (Exception e) {
					lsInstanceType = "DEVE";
				}

				// lsInstanceType = "ENGR"; // --------hardcoaded need to remove because above
				// query returns null;
				if (!Objects.equals(lsInstanceType, "ENGR")) {

					query = """
							SELECT Count(DISTINCT DCR_Number)
									  from Search_by_NavDB_Assignee
									 WHERE NavDB_id = ?
									   AND Effectivity_Cycle = ?
									   AND Data_Supplier = ?
									   AND DCR_Overall_Status = 'OPEN'
							""";
					rec = app.selectInto(query, custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
							dupl.getNewCycle(), global.getDataSupplier());
					lnCntDcr = rec.getInt();
					if (lnCntDcr > 1) {

						query = """
								SELECT COUNT(DISTINCT DCR_Number)
										    from Search_by_NavDB_Assignee
										   WHERE NavDB_id = ?
										     AND Effectivity_Cycle = ?
										     AND DCR_Number = ?
										     AND Data_Supplier = ?
										     AND DCR_Overall_Status = 'OPEN'
								""";
						rec = app.selectInto(query, custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
								dupl.getNewCycle(), global.getDcrNumber(), global.getDataSupplier());
						lnDcrNo = rec.getInt();
						if (Objects.equals(lnDcrNo, 1)) {
							alertDetails.getCurrent();
							if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
								moreButtons("C", "DCR",
										"Login DCR# " + global.getDcrNumber() + " is associated with NavDB '"
												+ custListBlk.getRow(system.getCursorRecordIndex()).getCustomer()
												+ "'.\n Do you want to copy record(s) using login DCR ? ",
										"Yes", "No", null);
								OracleHelpers.bulkClassMapper(displayAlert, this);
								alertDetails.createNewRecord("loginDcr1");
								throw new AlertException(event, alertDetails);
							} else {
								vSelect = alertDetails.getAlertValue("loginDcr1", alertDetails.getCurrentAlert());
							}
						}

						else {
							vSelect = 0;

						}

					}

					if (Objects.equals(vSelect, 1)) {
						custListBlk.getRow(system.getCursorRecordIndex())
								.setDcrNumber(Integer.parseInt(global.getDcrNumber()));
					}

					else {
						parameter.setProcessingCycle(dupl.getNewCycle());

						try {

							query = """
									SELECT DISTINCT DCR_Number from Search_by_Navdb_Assignee
												   WHERE NavDB_ID = ?
											   	   AND Effectivity_cycle = ?
												     AND DCR_overall_status = 'OPEN'
									""";
							rec = app.selectInto(query, custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
									dupl.getNewCycle());
							lnDcr = rec.getInt();
						}
						// NO_DATA_FOUND
						catch (NoDataFoundException e) {
							lnDcr = null;
						}

						if (Objects.equals(lnDcr, null)) {

							query = """
									select max(processing_cycle)
									           from pl_std_airport
									          where data_supplier = ?
									""";
							rec = app.selectInto(query, global.getDataSupplier());
							lnMaxCycle1 = rec.getInt();

							query = """
									select max(processing_cycle)
									           from pl_tld_airport
									          where data_supplier = ?
									""";
							rec = app.selectInto(query, global.getDataSupplier());
							lnMaxCycle2 = rec.getInt();
							if (lnMaxCycle1 > lnMaxCycle2) {
								lnMaxCycle = lnMaxCycle1;

							}

							else if (lnMaxCycle2 > lnMaxCycle1) {
								lnMaxCycle = lnMaxCycle2;

							}

							else {
								lnMaxCycle = lnMaxCycle2;

							}
							if (dupl.getNewCycle() > lnMaxCycle) {

								coreptLib.dspMsg(
										"Copying is not allowed, Cycle#" + dupl.getNewCycle() + " does not Exist ");
								throw new FormTriggerFailureException();
							}

							if (dupl.getNewCycle() > Integer.parseInt(global.getProcessingCycle())) {

								query = """
										select NVL(min(processing_cycle),0)
										              from pl_std_airport
												         where data_supplier = ?
										               and processing_cycle > ?
										""";
								rec = app.selectInto(query, global.getDataSupplier(), global.getProcessingCycle());
								lnNextCycle = rec.getInt();
								if (Objects.equals(lnNextCycle, 0)) {

									query = """
											select NVL(min(processing_cycle),0)
											                 from pl_tld_airport
													            where data_supplier = ?
											                  and processing_cycle > ?
											""";
									rec = app.selectInto(query, global.getDataSupplier(), global.getProcessingCycle());
									lnNextCycle = rec.getInt();

								}

								if (dupl.getNewCycle() > lnNextCycle) {

									coreptLib.dspMsg("No DCR exists for "
											+ custListBlk.getRow(system.getCursorRecordIndex()).getCustomer()
											+ " in Target Cycle " + dupl.getNewCycle());
									throw new FormTriggerFailureException();
								}
							}

							else if (dupl.getNewCycle() < Integer.parseInt(global.getProcessingCycle())) {
								lnOldCycle = app.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle", "util2",
										OracleTypes.NUMBER, new ProcedureInParameter("p_cycle",
												global.getProcessingCycle(), OracleTypes.NUMBER))
										.intValue();

								if (dupl.getNewCycle() < lnOldCycle) {
									coreptLib.dspMsg("No DCR exists for "
											+ custListBlk.getRow(system.getCursorRecordIndex()).getCustomer()
											+ " in Target Cycle " + dupl.getNewCycle());
									throw new FormTriggerFailureException();
								}
							}
							parameter.setProcessingCycle(dupl.getNewCycle());
							try {
								query = """
										SELECT DISTINCT DCR_Number from Search_by_Navdb_Assignee
													      WHERE NavDB_ID = ?
												   	      AND Effectivity_cycle = ?
													        AND DCR_overall_status = 'OPEN'
										""";
								rec = app.selectInto(query,
										custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
										dupl.getNewCycle());
								lnDcr = rec.getInt();
							}
							// NO_DATA_FOUND
							catch (NoDataFoundException e) {
								if (Integer.parseInt(global.getProcessingCycle()) < dupl.getNewCycle()) {
									parameter.setProcessingCycle(Integer.parseInt(global.getProcessingCycle()));

									try {

										query = """
												SELECT DISTINCT DCR_Number from Search_by_Navdb_Assignee
															            WHERE NavDB_ID = ?
															              AND Effectivity_cycle = ?
															              AND DCR_overall_status = 'OPEN'
												""";
										rec = app.selectInto(query,
												custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
												global.getProcessingCycle(), dupl.getNewCycle());
										lnDcr = rec.getInt();
									}
									// NO_DATA_FOUND
									catch (NoDataFoundException e1) {

										query = """
												select NVL(min(processing_cycle),0)
												                     from pl_std_airport
														                where data_supplier = ?
												                      and processing_cycle > ?
												""";
										rec = app.selectInto(query, global.getDataSupplier(), dupl.getNewCycle());
										lnProcCycle = rec.getInt();
										if (Objects.equals(lnProcCycle, 0)) {

											query = """
													select NVL(min(processing_cycle),0)
													                     from pl_tld_airport
															                where data_supplier = ?
													                      and processing_cycle > ?
													""";
											rec = app.selectInto(query, global.getDataSupplier(), dupl.getNewCycle());
											lnProcCycle = rec.getInt();

										}

										parameter.setProcessingCycle(lnProcCycle);

										try {

											query = """
													SELECT DISTINCT DCR_Number
																               from Search_by_Navdb_Assignee
																              WHERE NavDB_ID = ?
																                AND Effectivity_cycle = ?
																                AND DCR_overall_status = 'OPEN'
													""";
											rec = app.selectInto(query,
													custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
													lnProcCycle);
											lnDcr = rec.getInt();
										}
										// NO_DATA_FOUND
										catch (NoDataFoundException e2) {
											coreptLib.dspMsg("No DCR exists for "
													+ custListBlk.getRow(system.getCursorRecordIndex()).getCustomer()
													+ " in Cycle " + dupl.getNewCycle());
											throw new FormTriggerFailureException();
										}
									}
								}

								else if (Integer.parseInt(global.getProcessingCycle()) > dupl.getNewCycle()) {
									parameter
											.setProcessingCycle(app
													.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle",
															"util2", OracleTypes.NUMBER, new ProcedureInParameter(
																	"p_cycle", dupl.getNewCycle(), OracleTypes.NUMBER))
													.intValue());
									// :PARAMETER.PROCESSING_CYCLE := util2.get_previous_cycle(:dupl.new_cycle);
									try {

										query = """
												SELECT DISTINCT DCR_Number
													  		           from Search_by_Navdb_Assignee
															            WHERE NavDB_ID = ?
															              AND Effectivity_cycle = util2.get_previous_cycle(?)
															              AND DCR_overall_status = 'OPEN'
												""";
										rec = app.selectInto(query,
												custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
												dupl.getNewCycle());
										lnDcr = rec.getInt();
									} catch (NoDataFoundException e1) {
										parameter.setProcessingCycle(Integer.parseInt(global.getProcessingCycle()));

										try {

											query = """
													SELECT DISTINCT DCR_Number from Search_by_Navdb_Assignee
																              WHERE NavDB_ID = ?
																                AND Effectivity_cycle = ?
																                AND DCR_overall_status = 'OPEN'
													""";
											rec = app.selectInto(query,
													custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
													global.getProcessingCycle());
											lnDcr = rec.getInt();
										}

										catch (NoDataFoundException e2) {
											throw e2;
										}
									}
								}

								else if (Objects.equals(global.getProcessingCycle(), toString(dupl.getNewCycle()))) {
									if (Objects.equals(lnOldCycle, null)) {
										lnOldCycle = app
												.executeFunction(BigDecimal.class, "CPT", "get_previous_cycle", "util2",
														OracleTypes.NUMBER, new ProcedureInParameter("p_cycle",
																global.getProcessingCycle(), OracleTypes.NUMBER))
												.intValue();

									}
									parameter.setProcessingCycle(lnOldCycle);
									try {

										query = """
												SELECT DISTINCT DCR_Number from Search_by_Navdb_Assignee
															            WHERE NavDB_ID = ?
															              AND Effectivity_cycle = ?
															              AND DCR_overall_status = 'OPEN'
												""";
										rec = app.selectInto(query,
												custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
												lnOldCycle);
										lnDcr = rec.getInt();
									}
									// NO_DATA_FOUND
									catch (NoDataFoundException e1) {
										if (Objects.equals(lnNextCycle, 0)) {

											query = """
													SELECT MIN(CYCLE) from CYCLE WHERE CYCLE > ?
													""";
											rec = app.selectInto(query, global.getProcessingCycle());
											lnNextCycle = rec.getInt();
										}

										if (Objects.equals(lnNextCycle, 0)) {
											throw new NoDataFoundException(e1.getMessage());
										}

										else {
											parameter.setProcessingCycle(lnNextCycle);
										}

										query = """
												SELECT DISTINCT DCR_Number from Search_by_Navdb_Assignee
															              WHERE NavDB_ID = ?
															                AND Effectivity_cycle = ?
															                AND DCR_overall_status = 'OPEN'
												""";
										rec = app.selectInto(query,
												custListBlk.getRow(system.getCursorRecordIndex()).getCustomer(),
												lnNextCycle);
										lnDcr = rec.getInt();
									}
								}
							}
						}
						custListBlk.getRow(system.getCursorRecordIndex()).setDcrNumber(lnDcr);
					}
				} else {
					custListBlk.getRow(system.getCursorRecordIndex())
							.setDcrNumber(Integer.parseInt(global.getDcrNumber()));
				}
			} catch (NoDataFoundException e) {

				coreptLib.dspMsg("No DCR exists for " + custListBlk.getRow(system.getCursorRecordIndex()).getCustomer()
						+ " in Cycle " + dupl.getNewCycle());
				throw new FormTriggerFailureException();

			} catch (FormTriggerFailureException e) {
				throw e;
			}
			// err_no
			// catch (Exception e) {
			// throw new FormTriggerFailureException();
			// }
			catch (Exception e) {
				showLov("dcrList");
				if (!lsReturn) {
					coreptLib.dspMsg("Please select a DCR from the List.");
					throw new FormTriggerFailureException();
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" custListBlkDcrNumberWhenNewItemInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the custListBlkDcrNumberWhenNewItemInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	// Handled in UI
	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> custListBlkDcrNumberKeyNextItem(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" custListBlkDcrNumberKeyNextItem Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

//			if (Objects.equals((Boolean)getProperty.get("dupl.ident1st.enable"), true)) {
//				goItem("dupl.ident_1st");
//			} else {
//				// null;
//			}
//			if (Objects.equals(dupl.getDupRep(), "D")) {
//				if (Objects.equals((Boolean)getProperty.get("dupl.ident1st.enable"), true)) {
//					goItem("dupl.ident_1st");
//				}
//				else if (Objects.equals((Boolean)getProperty.get("dupl.newGateIdent.enable"), true)) {
//					goItem("dupl.new_gate_ident");
//				}
//				else {
//					goItem("dupl.duplicate");
//				}
//			}
//			else {
//				goItem("dupl.replace");
//			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" custListBlkDcrNumberKeyNextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the custListBlkDcrNumberKeyNextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> messageWhenNewBlockInstance(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" messageWhenNewBlockInstance Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			setWindowProperty("BASE_WINDOW", "windowState", NORMAL);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" messageWhenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the messageWhenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> messageCloseItWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" messageCloseItWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			global.setNewDcrNo("");
			pitssCon30.pc3dokey("exitForm");

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" messageCloseItWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the messageCloseItWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> messageSaveItWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" messageSaveItWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
			coreptMenuMmbServiceImpl.toolsExportFormData(this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			coreptLib.dspMsg("Sorry, please give an existing path and a file name with extension ''.txt''.");
			callForm("export_destination", "NO_HIDE", "NO_REPLACE", "NO_QUERY_ONLY", "SHARE_LIBRARY_DATA", "pl_id");
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<ExportDestinationTriggerResponseDto>> exportDestination(
			ExportDestinationTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<ExportDestinationTriggerResponseDto> responseObj = new BaseResponse<>();
		ExportDestinationTriggerResponseDto resDto = new ExportDestinationTriggerResponseDto();
		Record rec;
		BlockDetail mstBlockData = null;
		List<String> messageLogs = new ArrayList<>();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (lower(system.getCursorBlock()).equals("message")
					&& OracleHelpers.isNullorEmpty(selectOptions.getFileName())) {
				coreptLib.dspMsg("Sorry, please give an existing path and a file name with\nextension ''.txt''.");
				callForm("export_destination", "NO_HIDE", "NO_REPLACE", "NO_QUERY_ONLY", "SHARE_LIBRARY_DATA", "pl_id");
			}
			StringBuilder reportfile = new StringBuilder();
			String dateQuery = """
					     SELECT TO_CHAR(SYSDATE , 'Month DD,YYYY') as formatted_date  FROM DUAL
					""";

			String timeQuery = """
					SELECT  to_char(sysdate,'HH24:MI') FROM DUAL
					""";
			rec = app.selectInto(dateQuery);
			Record rec1 = app.selectInto(timeQuery);

			reportfile.append("Generated on ").append(rec.getObject()).append(" at ").append(rec1.getObject())
					.append("\n").append("\n");
			mstBlockData = reqDto.getExportDataBlocks().get("message");
			messageLogs = mstBlockData.getMessageLogs();
			for (int i = 0; i < messageLogs.size() - 1; i++) {
				reportfile.append(messageLogs.get(i)).append("\n");
			}
			// coverity-fixes
			log.info("" + messageLogs + mstBlockData);
			log.info("" + mstBlockData);

			OracleHelpers.ResponseMapper(this, resDto);
			String base64 = Base64.getEncoder().encodeToString(reportfile.toString().getBytes(StandardCharsets.UTF_8));
			ReportDetail reportDetail = new ReportDetail();
			reportDetail.setData(base64);
			resDto.setReport(reportDetail);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}

	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilDummyWhenButtonPressed(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilDummyWhenButtonPressed Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilDummyWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilDummyWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilClientinfoFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilClientinfoFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilFileFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilFileFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilHostFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilHostFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilSessionFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilSessionFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilFiletransferFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilFiletransferFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilOleFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilOleFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilCApiFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilCApiFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DuplicateRecordsTriggerResponseDto>> webutilWebutilBrowserFunctionsWhenCustomItemEvent(
			DuplicateRecordsTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DuplicateRecordsTriggerResponseDto> responseObj = new BaseResponse<>();
		DuplicateRecordsTriggerResponseDto resDto = new DuplicateRecordsTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the webutilWebutilBrowserFunctionsWhenCustomItemEvent Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void updateAppInstance() throws SQLException {
		super.displayAlert = this.displayAlert;
		OracleHelpers.bulkClassMapper(this, coreptLib);
		coreptLib.initialization(this);
		OracleHelpers.bulkClassMapper(this, displayAlert);
		OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		coreptMenuMmbServiceImpl.initialization(this);
		pitssCon30.setEvent(event);
		super.baseInstance = this;
		super.global = this.global;
		super.parameter = this.parameter;
		super.system = this.system;
		super.groups = this.groups;
		super.genericNativeQueryHelper = this.genericNativeQueryHelper;
		super.event = this.event;
		super.displayAlert = this.displayAlert;
		super.windows = this.windows;
	}
}
