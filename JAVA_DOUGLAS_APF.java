package com.honeywell.coreptdu.datatypes.douglasapf.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.airportapproach.entity.PlTldApproach;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStarLeg;
import com.honeywell.coreptdu.datatypes.coreptmenummb.serviceimpl.CoreptMenuMmbServiceImpl;
import com.honeywell.coreptdu.datatypes.corepttemplate.block.DisplayItemBlock;
import com.honeywell.coreptdu.datatypes.douglasapf.block.Webutil;
import com.honeywell.coreptdu.datatypes.douglasapf.dto.request.DouglasApfTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.douglasapf.dto.response.DouglasApfTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.douglasapf.entity.DouglasApf;
import com.honeywell.coreptdu.datatypes.douglasapf.entity.Md80SoftwareOptions;
import com.honeywell.coreptdu.datatypes.douglasapf.service.IDouglasApfTriggerService;
import com.honeywell.coreptdu.datatypes.exportdestination.block.SelectOptions;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.request.ExportDestinationTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.response.ExportDestinationTriggerResponseDto;
import com.honeywell.coreptdu.exception.AlertException;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.exception.FormTriggerFailureException;
import com.honeywell.coreptdu.exception.NoDataFoundException;
import com.honeywell.coreptdu.exception.UniqueConstraintViolationException;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.Global;
import com.honeywell.coreptdu.global.dto.Parameter;
import com.honeywell.coreptdu.global.dto.ReportDetail;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.global.dto.SystemDto;
import com.honeywell.coreptdu.global.forms.AlertDetail;
import com.honeywell.coreptdu.global.forms.BlockDetail;
import com.honeywell.coreptdu.global.forms.Event;
import com.honeywell.coreptdu.global.forms.FormConstant;
import com.honeywell.coreptdu.global.forms.WindowDetail;
import com.honeywell.coreptdu.pkg.body.RefreshMasterLibrary;
import com.honeywell.coreptdu.pkg.spec.IDisplayAlert;
import com.honeywell.coreptdu.pll.CoreptLib;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.CustomInteger;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.GenericNativeQueryHelper;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Block;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.HoneyWellUtils;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.PropertyHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroup;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequestScope
public class DouglasApfTriggerServiceImpl extends GenericTemplateForm<DouglasApfTriggerServiceImpl>
		implements IDouglasApfTriggerService{

	@Getter
	@Setter
	private Block<DouglasApf> douglasApf = new Block<>();
	@Getter
	@Setter
	private Md80SoftwareOptions md80SoftwareOptions = new Md80SoftwareOptions();
	@Getter
	@Setter
	private Webutil webutil = new Webutil();
	@Getter
	@Setter
	private DisplayItemBlock displayItemBlock = new DisplayItemBlock();
	@Getter
	@Setter
	private Global global = new Global();
	@Getter
	@Setter
	private SystemDto system = new SystemDto();
	@Getter
	@Setter
	private Parameter parameter = new Parameter();
	@Getter
	@Setter
	private Map<String, WindowDetail> windows = new HashMap<>();
	@Getter
	@Setter
	private List<Event> event = new ArrayList<>();
	@Getter
	@Setter
	private HashMap<String, RecordGroup> groups = new HashMap<>();
	@Getter
	@Setter
	private List<String> blocksOrder = new ArrayList<>();
	@Getter
	@Setter
	private AlertDetail alertDetails = new AlertDetail();
	@Autowired
	@Getter
	@Setter
	private IApplication app;
	@Getter
	@Setter
	private SelectOptions selectOptions = new SelectOptions();
	@Getter
	@Setter
	@Autowired
	private IDisplayAlert displayAlert;
	@Autowired
	private HashUtils hashUtils;
	@Autowired
	private CoreptLib coreptLib;
	@Autowired
	private GenericNativeQueryHelper genericNativeQueryHelper;
	@Autowired
	private RefreshMasterLibrary refreshMasterLibrary;
	@Autowired
	private CoreptMenuMmbServiceImpl coreptMenuMmbServiceImpl;

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> whenNewFormInstance(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" whenNewFormInstance Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			initializeForm();
			coreptLib.setBlock();
			displayItemBlock.setFormName("DOUGLAS APF");
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
	public void populateItems(String mname) throws Exception {
		log.info("populateItems Executing");
		String query = "";
		Record rec = null;
		try {
			String scrDescription = null;
			String scrPartNumber = null;
			displayItemBlock.setProcessingCycle(global.getProcessingCycle());
			List<String> getModeluInformation = coreptLib.getModuleInformation(mname, "", "");
			if (getModeluInformation != null && getModeluInformation.size() >= 2) {
				scrDescription = getModeluInformation.get(0);
				scrPartNumber = getModeluInformation.get(1);
			}
			if (Objects.equals(nameIn(this, "parameter.work_type"), "VIEW")) {
				displayItemBlock.setFormDesc(substr(scrDescription, 1, instr(scrDescription, "/") - 1)
						+ substr(scrDescription, instr(scrDescription, "/") + 7));

			} else {
				displayItemBlock.setFormDesc(scrDescription);
			}
			String date = "select to_char(sysdate,'DD-MON-YYYY HH24:MI:SS') from dual";
			rec = app.selectInto(date);
			displayItemBlock.setFormDateTime(rec.getString().toUpperCase());
			displayItemBlock.setFormName(mname);
			displayItemBlock.setFormPartNumber(scrPartNumber);
			try {
				query = "SELECT data_supplier_name from   data_supplier WHERE  data_supplier = nvl(?,'T')";
				rec = app.selectInto(query, global.getDataSupplier());
				displayItemBlock.setFormSource(rec.getString());
			} catch (NoDataFoundException e) {
				displayItemBlock.setFormSource(global.getDataSupplier());
			}
			query = "SELECT DECODE(nvl(?,'S'),'S','STANDARD','T','TAILORED','UNKNOWN')||', '||?||', '|| DECODE(nvl(?,'VIEW'),'VIEW','Query Only','Data Entry') from   dual";
			rec = app.selectInto(query, parameter.getRecordType(), parameter.getLibraryAccess(),
					parameter.getWorkType());
			displayItemBlock.setFormRecordType(rec.getString());
			log.info("populateItems Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing populateItems" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void initializeForm() throws Exception {
		log.info("initializeForm Executing");
		String query = "";
		Record rec = null;
		String fName = null;
		try {
			fName = getApplicationProperty(CURRENT_FORM_NAME);
			String vErr = null;
			defaultValue("J", "global.data_supplier");
			defaultValue("99999", "global.dcr_number");
			defaultValue(null, "global.processing_cycle");
			defaultValue("maximize", "global.status_window");
			defaultValue(null, "global.allroles");
			defaultValue("cpt", "global.user_name");
			defaultValue("cpt", "global.password");
			defaultValue("cpt", "global.connect_string");
			defaultValue("airport", "global.data_type");
			defaultValue("airport", "global.table_name");
			defaultValue("blank", "global.last_query");
			global.setUserName(app.getUserName());
			global.setPassword(app.getPassword());
			global.setConnectString(global.getConnectString());
			global.setStatusWindow(getWindowProperty(findWindow("DOUGLAS_APF", "baseWindow"), "windowState"));
			if (Objects.equals(nameIn(this, "global.status_window"), MAXIMIZE)) {
				setWindowProperty("baseWindow", "windowState", MAXIMIZE);
			} else {
				setWindowProperty("baseWindow", "windowState", NORMAL);
			}
			vErr = coreptLib.setRole(global.getAllroles());
			if (!Objects.equals(vErr, "PASSED")) {
				oneButton("S", "Fatal Error", "The roles cannot be activated.  Contact the COREPT Administrator.");
				exitForm();
			} else {
				if (Objects.equals(global.getProcessingCycle(), null)) {
					try {
						query = "select max(processing_cycle) from pl_std_airport";
						rec = app.selectInto(query);
						global.setProcessingCycle(rec.getString());
					} catch (NoDataFoundException e) {
						global.setProcessingCycle(null);
					}
				}
				global.setUserName(app.getUserName());
				global.setPassword(app.getPassword());
				global.setConnectString(global.getConnectString());
				setItemProperty("displayItemBlock.processingCycle", DISPLAYED, PROPERTY_TRUE);
				setItemProperty("displayItemBlock.processingCycle", WIDTH, "60");
				populateItems(fName);
				coreptLib.unsetQueryMenuItems();
			}
			setWindowProperty("baseWindow", TITLE, "Douglas APF");
			log.info("initializeForm Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing initializeForm" + e.getMessage());
			throw e;

		}
	}

	// TODO PUnits Manual configuration
	// ParentName ---> DSP_ERROR
	// File Name ---> sc_template.fmb
	// TODO PUnits Manual configuration
	// ParentName ---> DO_VALIDATE
	// File Name ---> sc_template.fmb

	@Override
	public void checkPackageFailure() throws Exception {
		log.info("checkPackageFailure Executing");
		// String query = "";
		// Record rec = null;
		try {
			/*
			 * if(!(formSuccess)) {
			 * throw new FormTriggerFailureException();
			 * }
			 */
			log.info("checkPackageFailure Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing checkPackageFailure" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void queryMasterDetails(Object relId, String detail) throws Exception {
		/***
		 * log.info("queryMasterDetails Executing");
		 * String query = "";
		 * Record rec = null;
		 * try {
		 * String oldmsg = null;
		 * String reldef = null;
		 * try {
		 * // TODO reldef = getRelationProperty(relId,deferredCoordination);
		 * oldmsg = system.getMessageLevel();
		 * if (Objects.equals(reldef, "FALSE")) {
		 * goBlock(detail, "");
		 * checkPackageFailure();
		 * system.setMessageLevel("10");
		 * // TODO Execute_Query;
		 * executeQuery(this, nameIn(this, "system.cursor_Block").toString(),
		 * nameIn(this, "").toString(), "","");
		 * system.setMessageLevel(oldmsg);
		 * } else {
		 * setBlockProperty(detail, "coordination_status", "non_coordinated");
		 * }
		 * } catch (FormTriggerFailureException e) {
		 * system.setMessageLevel(oldmsg);
		 * }
		 * log.info("queryMasterDetails Executed Successfully");
		 * } catch (Exception e) {
		 * log.error("Error while executing queryMasterDetails" + e.getMessage());
		 * throw e;
		 * 
		 * }
		 ***/
	}

	@Override
	public void clearAllMasterDetails() throws Exception {
		log.info("clearAllMasterDetails Executing");
		// String query = "";
		// Record rec = null;
		try {
			String mastblk = null;
			String coordop = null;
			String trigblk = null;
			String startitm = null;
			String frmstat = system.getFormStatus();
			String curblk = system.getCurrentBlock();
			String currel = null;
			String curdtl = null;
			/*
			 * FUNCTION First_Changed_Block_Below(Master VARCHAR2) RETURN VARCHAR2 IS curblk
			 * VARCHAR2(30); -- Current Block currel VARCHAR2(30); -- Current Relation
			 * retblk VARCHAR2(30); -- Return Block BEGIN -- -- Initialize Local Vars --
			 * curblk := Master; currel := Get_Block_Property(curblk,
			 * FIRST_MASTER_RELATION); -- -- While there exists another relation for this
			 * block -- WHILE currel IS NOT NULL LOOP -- -- Get the name of the detail block
			 * -- curblk := Get_Relation_Property(currel, DETAIL_NAME); -- -- If this block
			 * has changes, return its name -- IF ( Get_Block_Property(curblk, STATUS) =
			 * 'CHANGED' ) THEN RETURN curblk; ELSE -- -- No changes, recursively look for
			 * changed blocks below -- retblk := First_Changed_Block_Below(curblk); -- -- If
			 * some block below is changed, return its name -- IF retblk IS NOT NULL THEN
			 * RETURN retblk; ELSE -- -- Consider the next relation -- currel :=
			 * Get_Relation_Property(currel, NEXT_MASTER_RELATION); END IF; END IF; END
			 * LOOP;
			 * -- -- No changed blocks were found -- RETURN NULL; END
			 * First_Changed_Block_Below;
			 */
			try {
				mastblk = system.getMasterBlock();
				coordop = system.getCoordinationOperation();
				trigblk = system.getTriggerBlock();
				startitm = system.getCursorItem();
				frmstat = system.getFormStatus();
				if (!Arrays.asList("CLEAR_RECORD", "SYNCHRONIZE_BLOCKS").contains(coordop)) {
					if (Objects.equals(mastblk, trigblk)) {
						if (Objects.equals(frmstat, "CHANGED")) {
							// TODO curblk = firstChangedBlockBelow(mastblk);
							if (!Objects.equals(curblk, null)) {
								goBlock(curblk, "");
								checkPackageFailure();
								coreptLib.dspMsg("Please Save the change");
								checkToCommit("EXECUTE_QUERY");
								if (alertDetails.getTotalAlert() > 0) {
									system.setFormStatus("NORMAL");
								}
								coreptLib.coreptexecutequery(this);
								if (!(Objects.equals(system.getFormStatus(), "QUERY")
										|| Objects.equals(system.getBlockStatus(), "NEW"))) {
									throw new FormTriggerFailureException();
								}
							}
						}
					}
				}
				// TODO currel = getBlockProperty(trigblk,firstMasterRelation);
				currel = getBlockProperty(trigblk, "firstMasterRelation");
				while (!Objects.equals(currel, null)) {
					// TODO curdtl = getRelationProperty(currel,detailName);
					if (!Objects.equals(curdtl, null)) {
						if (!Objects.equals(getBlockProperty(curdtl, "status"), "NEW")) {
							goBlock(curdtl, "");
							checkPackageFailure();
							// TODO Clear_Block(NO_VALIDATE);
							clearBlock(currel, curdtl);
							if (!Objects.equals(system.getBlockStatus(), "NEW")) {
								throw new FormTriggerFailureException();
							}
						}
					}
					// TODO currel = getRelationProperty(currel,nextMasterRelation);
				}
				if (!Objects.equals(system.getCursorItem(), startitm)) {
					goItem(startitm);
					// TODO Check_Package_Failure --- Program Unit Calling
					checkPackageFailure();
				}
			} catch (FormTriggerFailureException e) {
				if (!Objects.equals(system.getCursorItem(), startitm)) {
					goItem(startitm);
				}
			}
			log.info("clearAllMasterDetails Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing clearAllMasterDetails" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void checkToCommit(String pActionType) throws Exception {
		log.info("checkToCommit Executing");
		try {
			Integer vButton = 0;
			Integer vNallowUpdate = 0;
			String vCblock = system.getCursorBlock();
			String fName = getApplicationProperty(CURRENT_FORM_NAME);
			String dataSupplier = null;
			String rowid = null;
            DouglasApfTriggerRequestDto reqDto = new DouglasApfTriggerRequestDto();
            OracleHelpers.ResponseMapper(this, reqDto);
			if (Objects.equals(system.getFormStatus(), "CHANGED")) {
				if (Objects.equals(pActionType, "COMMIT")) {
					alertDetails.getCurrent();
					if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						moreButtons("S", "Save Record", "You have modified record(s). Select an option: \n", "Save",
								"Cancel", "");
						alertDetails.createNewRecord("douglasAPFCheckToCommit1");
						throw new AlertException(event, alertDetails);
					} else {
						vButton = alertDetails.getAlertValue("douglasAPFCheckToCommit1",
								alertDetails.getCurrentAlert());
					}
					if (Objects.equals(vButton, 1)) {
						vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
								toInteger(global.getDcrNumber()), global.getDataSupplier(),
								toString(nameIn(this, vCblock + ".navdb_Id"))));
						if (Objects.equals(vNallowUpdate, 1)) {
							parameter.setUpdRec("N");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);
							
							if(!Objects.equals(md80SoftwareOptions.getNavdbId(), null) && 
									!Objects.equals(md80SoftwareOptions.getAirframeType(), null)) {
								md80SoftwareOptionsCreateDcrNumberWhenValidateItem();
								md80SoftwareOptionsWhenValidateRecord(reqDto);
							}
							
							rowid = getRowId();
							commitForm(this);
							sendUpdatedRowIdDetails();
//							if (!Objects.equals(rowid, null)) {
//								sendUpdatedRowIdDetails(rowid);
//							} 
							
							if(Objects.equals(md80SoftwareOptions.getRecordStatus(), "DELETED")) {
                            	md80SoftwareOptions = new Md80SoftwareOptions();
                            	md80SoftwareOptions.setRecordStatus("NEW");
							}
							
							coreptLib.dspMsg("Record has been saved successfully");
							system.setFormStatus("NORMAL");
							system.setRecordStatus("QUERIED");
						} else if (Objects.equals(vNallowUpdate, 0)) {
							parameter.setUpdRec("Y");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
							coreptLib.dspActionMsg("I", null, toInteger(nameIn(this, "global.dcr_number")),
									toInteger(global.getProcessingCycle()),
									toString(nameIn(this, toString(system.getCursorBlock() + ".navdb_Id"))));
						} else if (Objects.equals(vNallowUpdate, 2)) {
							switch (global.getDataSupplier()) {
								case "J":
									dataSupplier = "JEPPESEN";
									break;
								case "L":
									dataSupplier = "LIDO";
									break;
								case "E":
									dataSupplier = "NAVBLUE";
									break;
								case "Q":
									dataSupplier = "QUOVADIS";
									break;
								case "N":
									dataSupplier = "NAVERUS";
									break;
								case "C":
									dataSupplier = "CAST";
									break;
								default:
									break;
							}
							coreptLib.dspMsg(
									"Record Can't be Created as " + toString((nameIn(this, vCblock + ".navdb_Id")))
											+ " is not Associated with \nthe Login Supplier " + dataSupplier);
							throw new FormTriggerFailureException();
						}
					} else if (Objects.equals(vButton, 2)) {
						throw new FormTriggerFailureException();
					}
				} else if (Objects.equals(pActionType, "EXIT")) {

					alertDetails.getCurrent();
					if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						moreButtons("S", "Save Record", "You have modified record(s). Select an option: \n", "Save",
								"Exit", "Cancel");
						alertDetails.createNewRecord("douglasAPFCheckToCommit2");
						throw new AlertException(event, alertDetails);
					} else {
						vButton = alertDetails.getAlertValue("douglasAPFCheckToCommit2",
								alertDetails.getCurrentAlert());
					}
					if (Objects.equals(vButton, 1)) {
						vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
								toInteger(global.getDcrNumber()), global.getDataSupplier(),
								toString(nameIn(this, vCblock + ".navdb_Id"))));
						if (Objects.equals(vNallowUpdate, 1)) {
							parameter.setUpdRec("N");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);
							
							if(!Objects.equals(md80SoftwareOptions.getNavdbId(), null) && 
									!Objects.equals(md80SoftwareOptions.getAirframeType(), null)) {
								md80SoftwareOptionsCreateDcrNumberWhenValidateItem();
								md80SoftwareOptionsWhenValidateRecord(reqDto);

							}
							
							rowid = getRowId();
							commitForm(this);
							sendUpdatedRowIdDetails();
//							if (!Objects.equals(rowid, null)) {
//								sendUpdatedRowIdDetails(rowid);
//							} 
							
							coreptLib.dspMsg("Record has been saved successfully");
							system.setFormStatus("NORMAL");
							system.setRecordStatus("QUERIED");
							exitForm(fName, "doCommit", "");
						} else if (Objects.equals(vNallowUpdate, 0)) {
							parameter.setUpdRec("Y");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
							coreptLib.dspActionMsg("I", null, toInteger(nameIn(this, "global.dcr_number")),
									toInteger(global.getProcessingCycle()),
									toString(nameIn(this, toString(system.getCursorBlock() + ".navdb_Id"))));
						} else if (Objects.equals(vNallowUpdate, 2)) {
							switch (global.getDataSupplier()) {
								case "J":
									dataSupplier = "JEPPESEN";
									break;
								case "L":
									dataSupplier = "LIDO";
									break;
								case "E":
									dataSupplier = "NAVBLUE";
									break;
								case "Q":
									dataSupplier = "QUOVADIS";
									break;
								case "N":
									dataSupplier = "NAVERUS";
									break;
								case "C":
									dataSupplier = "CAST";
									break;
								default:
									break;
							}
							coreptLib.dspMsg(
									"Record Can't be Created as " + toString(nameIn(this, vCblock + ".navdb_Id"))
											+ " is not Associated with \nthe Login Supplier " + dataSupplier);
							throw new FormTriggerFailureException();
						}
					} else if (Objects.equals(vButton, 2)) {
						exitForm(fName, "noCommit", "");
					} else if (Objects.equals(vButton, 3)) {
						throw new FormTriggerFailureException();
					}
				} else if (Arrays.asList("EXECUTE_QUERY", "ENTER_QUERY").contains(pActionType)) {
					alertDetails.getCurrent();
					if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						moreButtons("S", "Save Record", "You have modified record(s). Select an option: \n", "Save",
								"Cancel", "");
						alertDetails.createNewRecord("douglasAPFCheckToCommit3");
						throw new AlertException(event, alertDetails);
					} else {
						vButton = alertDetails.getAlertValue("douglasAPFCheckToCommit3",
								alertDetails.getCurrentAlert());
					}
					if (Objects.equals(vButton, 1)) {
						vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
								toInteger(global.getDcrNumber()), global.getDataSupplier(),
								toString(nameIn(this, vCblock + ".navdb_Id"))));
						if (Objects.equals(vNallowUpdate, 1)) {
							parameter.setUpdRec("N");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);
							
							if(!Objects.equals(md80SoftwareOptions.getNavdbId(), null) && !Objects.equals(md80SoftwareOptions.getAirframeType(), null)) {
								md80SoftwareOptionsCreateDcrNumberWhenValidateItem();
								md80SoftwareOptionsWhenValidateRecord(reqDto);

							}
							
							rowid = getRowId();
							commitForm(this);
							sendUpdatedRowIdDetails();
//							if (!Objects.equals(rowid, null)) {
//								sendUpdatedRowIdDetails(rowid);
//							} 
							
                            if(Objects.equals(md80SoftwareOptions.getRecordStatus(), "DELETED")) {
                            	md80SoftwareOptions = new Md80SoftwareOptions();
                            	md80SoftwareOptions.setRecordStatus("NEW");
							}
                            
							coreptLib.dspMsg("Record has been saved successfully");
							system.setFormStatus("NORMAL");
							system.setRecordStatus("QUERIED");
							douglasApf.getData().clear();
						} else if (Objects.equals(vNallowUpdate, 0)) {
							parameter.setUpdRec("Y");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
							coreptLib.dspActionMsg("I", null, toInteger(nameIn(this, "global.dcr_number")),
									toInteger(global.getProcessingCycle()),
									toString(nameIn(this, toString(system.getCursorBlock() + ".navdb_Id"))));
						} else if (Objects.equals(vNallowUpdate, 2)) {
							switch (global.getDataSupplier()) {
								case "J":
									dataSupplier = "JEPPESEN";
									break;
								case "L":
									dataSupplier = "LIDO";
									break;
								case "E":
									dataSupplier = "NAVBLUE";
									break;
								case "Q":
									dataSupplier = "QUOVADIS";
									break;
								case "N":
									dataSupplier = "NAVERUS";
									break;
								case "C":
									dataSupplier = "CAST";
									break;
								default:
									break;
							}
							coreptLib.dspMsg(
									"Record Can't be Created as " + toString(nameIn(this, vCblock + ".navdb_Id"))
											+ " is not Associated with \nthe Login Supplier " + dataSupplier);
							throw new FormTriggerFailureException();
						}
					} else if (Objects.equals(vButton, 2)) {
						clearForm(fName, "noValidate", "");
						populateItems(fName);
						system.setFormStatus("NORMAL");
						douglasApf.getData().clear();
					}
				}
			}
			if (pActionType.equals("EXIT") && Objects.equals(parameter.getWorkType(), "VIEW")) {
				exitForm();
			}
			log.info("checkToCommit Executed Successfully");
		} catch (Exception e) {
			if (e.getMessage().contains("could not execute statement [ORA-00001: unique constraint")) {
				String message = new UniqueConstraintViolationException().getMessage();
				coreptLib.dspMsg(message + chr(10) + chr(10) + "Please check the exact error message from the \"Display"
						+ "\n" + " Error\" in the \"HELP\" menu");

			} else {

				log.error("Error while executing checkToCommit " + e.getMessage());
				throw e;
			}

		}
	}
	
	private String getRowId() throws Exception {
		log.info("getRowId Executing");
		String rowid = null;
		try {
			if (Arrays.asList("INSERT", "CHANGED", "DELETED").contains(md80SoftwareOptions.getRecordStatus())) {
				for (DouglasApf douglasApf : douglasApf.getData()) {
					if (Objects.equals(md80SoftwareOptions.getNavdbId(), douglasApf.getNavdbId())
							&& Objects.equals(md80SoftwareOptions.getAirframeType(), douglasApf.getAirframeType())) {
						rowid = douglasApf.getRowid();
						break;
					}
				}
			}
			log.info("getRowId Executed Successfully");
			return rowid;
		} catch (Exception e) {
			log.error("Error while executing getRowId " + e.getMessage());
			throw e;
		}
	}

	@Override
	public void checkSave() throws Exception {
		log.info("checkSave Executing");
		// String query = "";
		// Record rec = null;
		try {
			if (Arrays.asList("CHANGED", "NEW").contains(system.getFormStatus())) {
				if (Objects.equals(system.getBlockStatus(), "CHANGED")) {
					coreptLib.dspMsg("Please save your change first");
				}
				system.setBlockStatus("NORMAL");
				checkToCommit("COMMIT");
			}
			log.info("checkSave Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing checkSave " + e.getMessage());
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyEdit(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info("keyEdit executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				// editField;
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyEdit executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyEdit Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyDupItem(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info("keyEdit executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				duplicateItem("");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyDupItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyDupItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyDuprec(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDuprec Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				if (!Objects.equals(toString(nameIn(this, system.getCursorBlock() + ".navdb_Id")), null)) {
					setBlockProperty(system.getCursorBlock(), INSERT_ALLOWED, PROPERTY_TRUE);
					createRecord("douglasApf");
				}
				duplicateRecord("douglasApf", system.getCursorRecordIndex());
				copy(null, system.getCursorBlock() + ".airframe_type");
				copy(null, system.getCursorBlock() + ".create_dcr_number");
				copy(null, system.getCursorBlock() + ".update_dcr_number");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyDuprec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyDuprec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> onMessage(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onMessage Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			/***
			 * OracleHelpers.bulkClassMapper(reqDto, this); Integer msgnum = messageCode;
			 * String msgtxt = messageText; String msgtyp = messageType;
			 * 
			 * if((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) ||
			 * Objects.equals(msgnum, 40407))) { //TODO CLEAR_MESSAGE; //TODO
			 * dsp_msg("Record has been saved successfully"); } else
			 * if(Arrays.asList(41051,40350,47316,40353).contains(msgnum)) { null; } else
			 * if(Arrays.asList(40401,40405,40404).contains(msgnum)) { null; } else { //TODO
			 * display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
			 * "||msgtxt); throw new FormTriggerFailureException(); }
			 * OracleHelpers.ResponseMapper(this, resDto); log.info(" onMessage executed
			 * successfully");
			 ***/
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the onMessage Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> onError(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onError Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// Integer msgnum = null;
			String msgtxt = null;
			String msgtyp = null;
			String vCblock = system.getCursorBlock();
			Integer vNallowUpdate = 0;
			if ((Objects.equals(global.getErrorCode(), 40400) || Objects.equals(global.getErrorCode(), 40406)
					|| Objects.equals(global.getErrorCode(), 40407))) {
				message("changes saved successfully");
			} else if (Arrays.asList(41051, 40350, 47316, 40353).contains(global.getErrorCode())) {
				return null;
			} else if (Arrays.asList(40401, 40405).contains(global.getErrorCode())) {
				return null;
			} else if (Objects.equals(global.getErrorCode(), 40100)) {
				clearMessage();
				message("at the first record.");
			} else if (Objects.equals(global.getErrorCode(), 40735) && like("01031", toString(global.getErrorCode()))) {
				clearMessage();
				coreptLib.dspMsg(global.getErrorCode().toString() + " Insufficient privileges. ");
			} else if (Arrays.asList(40508, 40509).contains(global.getErrorCode())) {
				coreptLib.dspMsg(msgtxt + chr(10) + chr(10)
						+ "Please check the exact error message from the \"Display Error\" in the \"HELP\" menu");
			} else if (Arrays.asList(40200).contains(global.getErrorCode())) {
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
						if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
							vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
									toInteger(global.getDcrNumber()), global.getDataSupplier(),
									toString(nameIn(this, vCblock + ".navdb_id"))));
							if (Objects.equals(vNallowUpdate, 1)) {
								parameter.setUpdRec("N");
								setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);
							} else if (Objects.equals(vNallowUpdate, 0)) {
								parameter.setUpdRec("Y");
								setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
								coreptLib.dspActionMsg("U", null, toInteger(nameIn(this, "global.dcr_number")),
										toInteger(global.getProcessingCycle()),
										toString(nameIn(this, vCblock + ".navdb_id")));
							} else if (Objects.equals(vNallowUpdate, 2)) {
								String dataSupplier = null;
								switch (global.getDataSupplier()) {
									case "J":
										dataSupplier = "JEPPESEN";
										break;
									case "L":
										dataSupplier = "LIDO";
										break;
									case "E":
										dataSupplier = "NAVBLUE";
										break;
									case "Q":
										dataSupplier = "QUOVADIS";
										break;
									case "N":
										dataSupplier = "NAVERUS";
										break;
									case "C":
										dataSupplier = "CAST";
										break;
									default:
										break;
								}
								coreptLib.dspMsg(
										"Record Can't be Updated as " + toString(nameIn(this, vCblock + ".navdb_Id"))
												+ " is not Associated with \nthe Login Supplier " + dataSupplier);
								throw new FormTriggerFailureException();
							}
						}
					}
				}
			} else {
				displayAlert.oneButton("S", "Error", msgtyp + "-" + toChar(global.getErrorCode()) + ": " + msgtxt);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" onError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the onError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyExit(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExit Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getFormStatus(), "CHANGED") && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				checkToCommit("EXIT");
			} else {
				setApplicationProperty(PROPERTY_FALSE, CURRENT_FORM_NAME);
				exitForm("DOUGLAS_APF", "noValidate", "");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyExit executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyExit Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyCrerec(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCrerec Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				parameter.setUpdRec(coreptLib.setActionRestrSel(toString(nameIn(this, "system.cursor_Block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "CRE"));
				createRecord("douglasApf");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyCrerec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> onClearDetails(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onClearDetails Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			clearAllMasterDetails();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" onClearDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the onClearDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyEntqry(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyEntqry Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getCursorBlock(), "MD80_SOFTWARE_OPTIONS")) {
				goBlock("douglasApf", "navdbId");
			}
			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				checkToCommit("ENTER_QUERY");
			}
			system.setMode("ENTER_QUERY");
			system.setFormStatus("NORMAL");
			coreptLib.coreptenterquery();
			if (Objects.equals(system.getMode(), "NORMAL")) {
				coreptLib.unsetQueryMenuItems();
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyEntqry executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyEntqry Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyExeqry(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" keyExeqry Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getCursorBlock(), "md80SoftwareOptions")) {
				goBlock("douglasApf", "navdbId");
			}
			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				checkToCommit("EXECUTE_QUERY");
			}

			if (!Objects.equals(system.getMode(), "ENTER_QUERY")) {
				douglasApf.getData().clear();
				douglasApf.add(new DouglasApf());
				system.setCursorRecordIndex(First_Record);
			}
			
			coreptLib.coreptexecutequery(this);
			if (!system.getFormStatus().equals("NEW")) {
				global.setCreateDcrNumber(toString(nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyExeqry executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyExeqry Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> keyCommit(DouglasApfTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCommit Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			
			if (global.getClearBlock()) {
				String rowid = getRowId();
				commitForm(this);
				sendUpdatedRowIdDetails();
				if (!Objects.equals(rowid, null)) {
					sendUpdatedRowIdDetails(rowid);
				} 

				global.setClearBlock(false);
				coreptLib.dspMsg("Record has been saved successfully");
				system.setFormStatus("NORMAL");
			} else if (Objects.equals(parameter.getWorkType(), "VIEW")) {
//				 null;
			} else {
				checkToCommit("COMMIT");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyCommit executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyCommit Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfOnPopulateDetails(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfOnPopulateDetails Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String recstat = system.getRecordStatus();
			String startitm = system.getCursorItem();
			Object relId = null;
			if ((Objects.equals(recstat, "NEW") || Objects.equals(recstat, "INSERT"))) {
				return null;
			}
			if (((!Objects.equals(douglasApf.getRow(system.getCursorRecordIndex()).getNavdbId(), null))
					|| (!Objects.equals(douglasApf.getRow(system.getCursorRecordIndex()).getAirframeType(), null)))) {
				// TODO relId = findRelation("DOUGLAS_APF.DOUGLAS_APF_MD80_SOFTWARE_");
				// TODO Query_Master_Details(rel_id,'MD80_SOFTWARE_OPTIONS') --- Program Unit
				queryMasterDetails(relId, "md80SoftwareOptions");
			}
			if ((!Objects.equals(system.getCursorItem(), startitm))) {
				goItem(startitm);
				checkPackageFailure();
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfOnPopulateDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfOnPopulateDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void douglasApfOnCheckDeleteMaster() throws Exception {
		log.info(" douglasApfOnCheckDeleteMaster Executing");
		try {
			// PreparedStatement prepareStatement = null;
			// String dummyDefine = null;
			int rec = 0;
			String md80SoftwareOptionsCur = "SELECT 1 FROM MD80_SOFTWARE_OPTIONS C WHERE C.NAVDB_ID = ? and C.AIRFRAME_TYPE = ?";
//			List<Record> records = app.executeQuery(md80SoftwareOptionsCur);
			rec = app.executeNonQuery(md80SoftwareOptionsCur, douglasApf.getRow(system.getCursorRecordIndex()).getNavdbId(), douglasApf.getRow(system.getCursorRecordIndex()).getAirframeType());

			if (rec != 0) {
				message("cannot delete master record when matching detail records exist.");
				throw new FormTriggerFailureException();
			}
			log.info(" douglasApfOnCheckDeleteMaster executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfOnCheckDeleteMaster Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfWhenValidateRecord(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfWhenValidateRecord Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			try {
				coreptLib.setActionRestrSel(toString(nameIn(this, "system.cursor_Block")), global.getDataSupplier(),
						toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
						parameter.getRecordType(), "CRE");
				event.clear();
			} catch (Exception e) {
				event.clear();
				OracleHelpers.ResponseMapper(this, resDto);
				log.info(" douglasApfWhenValidateRecord executed successfully");
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
			}
			
			CustomInteger dcrNumber = new CustomInteger(global.getDcrNumber());
			douglasApf.getRow(system.getCursorRecordIndex()).setUpdateDcrNumber(dcrNumber);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfKeyDelrec(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfKeyDelrec Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vNallowUpdate = 0;
			String dataSupplier = null;
			String vCblock = system.getCursorBlock();
			Integer vButton = null;
			// Record rec = null;
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				if (!Arrays.asList("INSERT", "NEW").contains(system.getRecordStatus())) {
					if (!Objects.equals(md80SoftwareOptions.getCreateDcrNumber(), null)) {
						vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
								toInteger(global.getDcrNumber()), global.getDataSupplier(),
								toString(nameIn(this, vCblock + ".navdb_Id"))));
						if (Objects.equals(vNallowUpdate, 1)) {
							parameter.setUpdRec("N");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);

							alertDetails.getCurrent();
							if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
								moreButtons("S", "Delete Record",
										"Please be sure you want to delete the current record and \nits associated detail record."
												+ chr(10) + " ",
										"Delete All", "Cancel", "");
								alertDetails.createNewRecord("douglasApfKeyDelrec1");
								throw new AlertException(event, alertDetails);
							} else {
								vButton = alertDetails.getAlertValue("douglasApfKeyDelrec1",
										alertDetails.getCurrentAlert());
							}
							if (Objects.equals(vButton, 1)) {
								
								if (!Objects.equals(md80SoftwareOptions.getAirframeType(), null)
										&& !Objects.equals(md80SoftwareOptions.getNavdbId(), null)) {
									md80SoftwareOptions.setRecordStatus("DELETED");
//									goBlock("md80SoftwareOptions", "createDcrNumber");
									commitForm(this);
									String _rowid =  toString(nameIn(this,"douglas_apf.rowid"));
									sendUpdatedRowIdDetails(_rowid);
									md80SoftwareOptions = new Md80SoftwareOptions();
								}
								douglasApfOnCheckDeleteMaster();
								douglasApf.getRow(system.getCursorRecordIndex()).setRecordStatus("DELETED");
//								goBlock("douglasApf", "navdbId");
								commitForm(this);
								String _rowid =  toString(nameIn(this,"douglas_apf.rowid"));
								sendUpdatedRowIdDetails(_rowid);
								coreptLib.dspMsg("Record has been saved successfully");
							}
						} else if (Objects.equals(vNallowUpdate, 0)) {
							parameter.setUpdRec("Y");
							setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
							coreptLib.dspActionMsg("D", null, toInteger(nameIn(this, "global.dcr_number")),
									toInteger(global.getProcessingCycle()),
									toString(nameIn(this, toString(system.getCursorBlock() + ".navdb_Id"))));
						} else if (Objects.equals(vNallowUpdate, 2)) {
							switch (global.getDataSupplier()) {
								case "J":
									dataSupplier = "JEPPESEN";
									break;
								case "L":
									dataSupplier = "LIDO";
									break;
								case "E":
									dataSupplier = "NAVBLUE";
									break;
								case "Q":
									dataSupplier = "QUOVADIS";
									break;
								case "N":
									dataSupplier = "NAVERUS";
									break;
								case "C":
									dataSupplier = "CAST";
									break;
								default:
									break;
							}
							coreptLib.dspMsg(
									"Record Can't be Deleted as " + toString(nameIn(this, vCblock + ".navdb_Id"))
											+ " is not Associated with \nthe Login Supplier " + dataSupplier);
							throw new FormTriggerFailureException();
						}
					} else {
						if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
							vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
									toInteger(global.getDcrNumber()), global.getDataSupplier(),
									toString(nameIn(this, vCblock + ".navdb_Id"))));
							if (Objects.equals(vNallowUpdate, 1)) {
								parameter.setUpdRec("N");
								setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);

								alertDetails.getCurrent();
								if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
									moreButtons("S", "Delete Record",
											"You are gong to delete this record. Please be sure. \n", "Delete It",
											"Cancel", "");
									alertDetails.createNewRecord("douglasApfKeyDelRec2");
									throw new AlertException(event, alertDetails);
								} else {
									vButton = alertDetails.getAlertValue("douglasApfKeyDelRec2",
											alertDetails.getCurrentAlert());
								}

								if (Objects.equals(vButton, 1)) {
									
									if (!Objects.equals(md80SoftwareOptions.getAirframeType(), null)
											&& !Objects.equals(md80SoftwareOptions.getNavdbId(), null)) {
										md80SoftwareOptions.setRecordStatus("DELETED");
//										goBlock("md80SoftwareOptions", "createDcrNumber");
										commitForm(this);
										String _rowid =  toString(nameIn(this,"douglas_apf.rowid"));
										sendUpdatedRowIdDetails(_rowid);
										md80SoftwareOptions = new Md80SoftwareOptions();
									}
									douglasApfOnCheckDeleteMaster();
									douglasApf.getRow(system.getCursorRecordIndex()).setRecordStatus("DELETED");
//									goBlock("douglasApf", "navdbId");
									commitForm(this);
									sendUpdatedRowIdDetails();
//									String _rowid =  toString(nameIn(this,"douglas_apf.rowid"));
//									sendUpdatedRowIdDetails(_rowid);
									coreptLib.dspMsg("Record has been saved successfully");
								} else {
									throw new FormTriggerFailureException();
								}
							} else if (Objects.equals(vNallowUpdate, 0)) {
								parameter.setUpdRec("Y");
								setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
								coreptLib.dspActionMsg("D", null, toInteger(nameIn(this, "global.dcr_number")),
										toInteger(global.getProcessingCycle()),
										toString(nameIn(this, toString(system.getCursorBlock() + ".navdb_Id"))));
							} else if (Objects.equals(vNallowUpdate, 2)) {
								switch (global.getDataSupplier()) {
									case "J":
										dataSupplier = "JEPPESEN";
										break;
									case "L":
										dataSupplier = "LIDO";
										break;
									case "E":
										dataSupplier = "NAVBLUE";
										break;
									case "Q":
										dataSupplier = "QUOVADIS";
										break;
									case "N":
										dataSupplier = "NAVERUS";
										break;
									case "C":
										dataSupplier = "CAST";
										break;
									default:
										break;
								}
								coreptLib.dspMsg(
										"Record Can't be Deleted as " + toString(nameIn(this, vCblock + ".navdb_Id"))
												+ " is not Associated with \nthe Login Supplier " + dataSupplier);
								throw new FormTriggerFailureException();
							}
						}
					}
				} else {
					deleteRecord("douglasApf");
					
					system.setRecordStatus("DELETED");
					system.setFormStatus("CHANGED");
				}

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfKeyDelrec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfKeyDelrec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfWhenNewRecordInstance(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfWhenNewRecordInstance Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vNallowUpdate = 0;
			String vCblock = system.getCursorBlock();
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), global.getDataSupplier(),
						toString(nameIn(this, vCblock + ".navd_bId"))));
				if (Objects.equals(vNallowUpdate, 1)) {
					parameter.setUpdRec("N");
					setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);
				} else {
					parameter.setUpdRec("Y");
					setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfWhenNewBlockInstance(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfWhenNewBlockInstance Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			checkSave();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfWhenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfWhenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfNavdbIdWhenValidateItem(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfNavdbIdWhenValidateItem Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(douglasApf.getRow(system.getCursorRecordIndex()).getNavdbId(), null)) {
				Integer vCount = 0;
				Integer lsCnt = 0;
				String query = null;
				Record rec = null;
				query = "select count(*) from navdb where navdb_id = ?";
				rec = app.selectInto(query, douglasApf.getRow(system.getCursorRecordIndex()).getNavdbId());
				vCount = rec.getInt();
				if (Objects.equals(vCount, 0)) {
					try {
						// coreptLib.dspMsg("Invalid NAVDB ID, please correct it.");
						throw new FormTriggerFailureException();
					} catch (Exception e) {
						listValues("navdbId");
						throw e;
					}
				}
				query = "SELECT count(*) from fms_data_type WHERE fms_id = ( SELECT fms_id FROM navdb WHERE navdb_id = ? ) AND record_type_code = ( SELECT record_type_code FROM record_type WHERE record_type_descr = 'DOUGLAS APF')";
				rec = app.selectInto(query, douglasApf.getRow(system.getCursorRecordIndex()).getNavdbId());
				lsCnt = rec.getInt();
				if (Objects.equals(lsCnt, 0)) {
					oneButton("S", "APF Alert", "This NavDB is not associated with Douglas APF data type");
					throw new FormTriggerFailureException();
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfNavdbIdWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfNavdbIdWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfAirframeTypeWhenValidateItem(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfAirframeTypeWhenValidateItem Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(rtrim(douglasApf.getRow(system.getCursorRecordIndex()).getAirframeType()), null)) {
				coreptLib.dspMsg("Required field, please enter value.");
				throw new FormTriggerFailureException();
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfAirframeTypeWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfAirframeTypeWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> douglasApfCreateDcrNumberWhenValidateItem(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" douglasApfCreateDcrNumberWhenValidateItem Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		String query = null;
		Record rec = null;
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(rtrim(toString(douglasApf.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())),
					null)) {
				query = "select dcr_number_seq.nextval from dual";
				rec = app.selectInto(query);
				CustomInteger dcrNumber = new CustomInteger(toString(rec.getInt()));
				douglasApf.getRow(system.getCursorRecordIndex()).setCreateDcrNumber(dcrNumber);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" douglasApfCreateDcrNumberWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the douglasApfCreateDcrNumberWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> md80SoftwareOptionsKeyDelrec(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" md80SoftwareOptionsKeyDelrec Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vNallowUpdate = 0;
			String vCblock = system.getCursorBlock();
			Integer vButton = null;
			String dataSupplier = null;

			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), global.getDataSupplier(),
							toString(nameIn(this, vCblock + ".navdb_Id"))));
					if (Objects.equals(vNallowUpdate, 1)) {
						parameter.setUpdRec("N");
						setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);

						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
							moreButtons("S", "Delete Record", "You are going to delete this record. Please be sure. \n",
									"Delete It", "Cancel", "");
							alertDetails.createNewRecord("md80SoftwareOptionsKeyDelrec");
							throw new AlertException(event, alertDetails);
						} else {
							vButton = alertDetails.getAlertValue("md80SoftwareOptionsKeyDelrec",
									alertDetails.getCurrentAlert());
						}
						if (Objects.equals(vButton, 1)) {
							deleteRecord("md80SoftwareOptions");
							copy("DELETED", system.getCursorBlock() + ".record_status");
							system.setFormStatus("CHANGED");
							String _rowid =  null;
							for(DouglasApf douglasApf : douglasApf.getData()){
								if(Objects.equals(md80SoftwareOptions.getNavdbId(), douglasApf.getNavdbId()) &&
										Objects.equals(md80SoftwareOptions.getAirframeType(), douglasApf.getAirframeType())) {
									_rowid =  douglasApf.getRowid();
								}
							}
							sendLockRowIdDetails(_rowid);
							
							system.setRecordStatus("DELETED");
						}
					} else if (Objects.equals(vNallowUpdate, 0)) {
						parameter.setUpdRec("Y");
						setBlockProperty(vCblock, FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_FALSE);
						coreptLib.dspActionMsg("D", null, toInteger(nameIn(this, "global.dcr_number")),
								toInteger(global.getProcessingCycle()),
								toString(nameIn(this, toString(system.getCursorBlock() + ".navdb_Id"))));
					} else if (Objects.equals(vNallowUpdate, 2)) {
						switch (global.getDataSupplier()) {
							case "J":
								dataSupplier = "JEPPESEN";
								break;
							case "L":
								dataSupplier = "LIDO";
								break;
							case "E":
								dataSupplier = "NAVBLUE";
								break;
							case "Q":
								dataSupplier = "QUOVADIS";
								break;
							case "N":
								dataSupplier = "NAVERUS";
								break;
							case "C":
								dataSupplier = "CAST";
								break;
							default:
								break;
						}
						coreptLib.dspMsg("Record Can't be Deleted as " + toString(nameIn(this, vCblock + ".navdb_Id"))
								+ " is not Associated with \nthe Login Supplier " + dataSupplier);
						throw new FormTriggerFailureException();
					}
				} else {
					deleteRecord("md80SoftwareOptions");
					
					system.setRecordStatus("DELETED");
					system.setFormStatus("CHANGED");
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" md80SoftwareOptionsKeyDelrec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the md80SoftwareOptionsKeyDelrec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> md80SoftwareOptionsWhenValidateRecord(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" md80SoftwareOptionsWhenValidateRecord Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			CustomInteger dcrNumber = new CustomInteger(global.getDcrNumber());
			md80SoftwareOptions.setUpdateDcrNumber(dcrNumber);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" md80SoftwareOptionsWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the md80SoftwareOptionsWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> md80SoftwareOptionsWhenNewRecordInstance(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" md80SoftwareOptionsWhenNewRecordInstance Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (toInteger(system.getTriggerRecord()) > 1) {
				previousRecord("md80SoftwareOptions");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" md80SoftwareOptionsWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the md80SoftwareOptionsWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> md80SoftwareOptionsPostQuery(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" md80SoftwareOptionsPostQuery Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				return null;
			} else {
				Integer vNallowUpdate = 0;
				String vCblock = system.getCursorBlock();
				vNallowUpdate = toInteger(coreptLib.checkValidNavdb(toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), global.getDataSupplier(),
						toString(nameIn(this, "douglas_Apf.navdb_Id"))));
				if (Objects.equals(vNallowUpdate, 1)) {
					parameter.setUpdRec("N");
					setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_TRUE);
				} else if (Arrays.asList(0, 2).contains(vNallowUpdate)) {
					parameter.setUpdRec("Y");
					setBlockProperty(vCblock, UPDATE_ALLOWED, PROPERTY_FALSE);
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" md80SoftwareOptionsPostQuery executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the md80SoftwareOptionsPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void md80SoftwareOptionsCreateDcrNumberWhenValidateItem() throws Exception{
		String query = null;
		Record rec = null;
		if (Objects.equals(rtrim(toString(md80SoftwareOptions.getCreateDcrNumber())), null)) {
			query = "select dcr_number_seq.nextval from dual";
			rec = app.selectInto(query);
			CustomInteger dcrNumber = new CustomInteger(toString(rec.getInt()));
			md80SoftwareOptions.setCreateDcrNumber(dcrNumber);
		}
	}
	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> md80SoftwareOptionsCreateDcrNumberWhenValidateItem(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" md80SoftwareOptionsCreateDcrNumberWhenValidateItem Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			String query = null;
			Record rec = null;
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(rtrim(toString(md80SoftwareOptions.getCreateDcrNumber())), null)) {
				query = "select dcr_number_seq.nextval from dual";
				rec = app.selectInto(query);
				CustomInteger dcrNumber = new CustomInteger(toString(rec.getInt()));
				md80SoftwareOptions.setCreateDcrNumber(dcrNumber);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" md80SoftwareOptionsCreateDcrNumberWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the md80SoftwareOptionsCreateDcrNumberWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> displayItemBlockFormPartNumberWhenNewItemInstance(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" displayItemBlockFormPartNumberWhenNewItemInstance Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockFormPartNumberWhenNewItemInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockFormPartNumberWhenNewItemInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilDummyWhenButtonPressed(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilDummyWhenButtonPressed Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilClientinfoFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilFileFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilHostFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilSessionFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilFiletransferFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilOleFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilCApiFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> webutilWebutilBrowserFunctionsWhenCustomItemEvent(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
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

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> toolsDuplicate(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
			coreptMenuMmbServiceImpl.toolsDuplicate(this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error(
					"Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}

	}

	@Override
	public ResponseEntity<ResponseDto<DouglasApfTriggerResponseDto>> toolsExportDestination(
			DouglasApfTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<DouglasApfTriggerResponseDto> responseObj = new BaseResponse<>();
		DouglasApfTriggerResponseDto resDto = new DouglasApfTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
			coreptMenuMmbServiceImpl.toolsExportFormData(this);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
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
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(reqDto, this);
			BlockDetail mstBlockData = null;
			BlockDetail childBlockData = null;

			// String Builders
			StringBuilder reportfile = new StringBuilder();
			List<Record> recs = null;
			// Master Block
			if (lower(system.getCursorBlock()).equals("message")
					&& OracleHelpers.isNullorEmpty(selectOptions.getFileName())) {
				coreptLib.dspMsg("Sorry, please give an existing path and a file name with\nextension ''.txt''.");
				throw new FormTriggerFailureException();
			}
			if (OracleHelpers.isNullorEmpty(selectOptions.getFileName())) {
				PropertyHelpers.setAlertProperty(event, "dsp_msg", "stop", "Forms",
						"WUT-130: Client file name cannot be null", "ALERT_MESSAGE_TEXT", "OK", null, null);
				PropertyHelpers.setShowAlert(event, "dsp_msg", false);
				throw new FormTriggerFailureException();
			}
			if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("md80SoftwareOptions")) {
				mstBlockData = reqDto.getExportDataBlocks().get("md80SoftwareOptions");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				Record mstRec = app.selectInto(query);
				Md80SoftwareOptions plTldAirportMr = app.mapResultSetToClass(mstRec, Md80SoftwareOptions.class);
				reportfile.append(getExportData(plTldAirportMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));

			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("douglasApf")) {
				mstBlockData = reqDto.getExportDataBlocks().get("douglasApf");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (upper(query).contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				// Child Block
				childBlockData = reqDto.getExportDataBlocks().get("md80SoftwareOptions");
				List<String> childPromptNames = getBlockMetaData(childBlockData, "PROMPT_NAME");
				List<String> childDatabseColumns = getBlockMetaData(childBlockData, "DATABASE_COLUMN");
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				reportfile.append(getExportHeader(childPromptNames, 1, selectOptions.getDelimiter()));

				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					DouglasApf douglasApf = app.mapResultSetToClass(mstRec, DouglasApf.class);
					reportfile.append(getExportData(douglasApf, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
					// Fetching the Detail Blocks
					String childQuery = """
							Select * From MD80_SOFTWARE_OPTIONS
							    where
							   ? = NAVDB_ID AND
							? = AIRFRAME_TYPE
							 """;
					List<Record> childRecs = app.executeQuery(childQuery, douglasApf.getNavdbId(),
							douglasApf.getAirframeType());
					reportfile.append(getChildExportData(childRecs, childDatabseColumns, 1, "md80SoftwareOptions"));
				}
			} else if (lower(system.getCursorBlock()).equals("message")) {
				List<String> messageLogs = new ArrayList<>();
				String dateQuery = """
						     SELECT TO_CHAR(SYSDATE , 'Month DD,YYYY') as formatted_date  FROM DUAL
						""";

				String timeQuery = """
						SELECT  to_char(sysdate,'HH24:MI') FROM DUAL
						""";
				Record dateRec = app.selectInto(dateQuery);
				Record timeRec = app.selectInto(timeQuery);

				reportfile.append("Generated on ").append(dateRec.getObject()).append(" at ")
						.append(timeRec.getObject()).append("\n").append("\n");
				mstBlockData = reqDto.getExportDataBlocks().get("message");
				messageLogs = mstBlockData.getMessageLogs();
				for (int i = 0; i <= messageLogs.size() - 1; i++) {
					reportfile.append(messageLogs.get(i)).append("\n");
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			String base64 = Base64.getEncoder().encodeToString(reportfile.toString().getBytes(StandardCharsets.UTF_8));
			ReportDetail reportDetail = new ReportDetail();
			reportDetail.setData(base64);
			resDto.setReport(reportDetail);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (RuntimeException e) {
			log.info("RuntimeException captured");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}

	}
	
	public String getChildExportData(List<Record> recs, List<String> columns, int depth,
			String childBlockName) throws Exception {
		StringBuilder data = new StringBuilder();
		if (recs.size() <= 0) {
			return "";
		}
		// Reset Blocks
		md80SoftwareOptions = new Md80SoftwareOptions();
		for (Record rec : recs) {
			if ("md80SoftwareOptions".equals(childBlockName)) {
				Md80SoftwareOptions md80SoftwareOptions = app.mapResultSetToClass(rec, Md80SoftwareOptions.class);
				data.append(getExportData(md80SoftwareOptions, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}
		}
		return data.toString();

	}
	

	public void updateAppInstance() {
		OracleHelpers.bulkClassMapper(this, displayAlert);
		OracleHelpers.bulkClassMapper(this, coreptLib);
		OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		coreptMenuMmbServiceImpl.initialization(this);
		coreptLib.initialization(this);
		super.app = this.app;
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


---------------------------

package com.honeywell.coreptdu.datatypes.douglasapf.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.douglasapf.dto.request.Md80SoftwareOptionsQuerySearchDto;
import com.honeywell.coreptdu.datatypes.douglasapf.dto.request.Md80SoftwareOptionsRequestDto;
import com.honeywell.coreptdu.datatypes.douglasapf.entity.Md80SoftwareOptions;
import com.honeywell.coreptdu.datatypes.douglasapf.entity.idclass.Md80SoftwareOptionsIdClass;
import com.honeywell.coreptdu.datatypes.douglasapf.repository.IMd80SoftwareOptionsRepository;
import com.honeywell.coreptdu.datatypes.douglasapf.service.IMd80SoftwareOptionsService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * Md80SoftwareOptions Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class Md80SoftwareOptionsServiceImpl implements IMd80SoftwareOptionsService {

	@Autowired
	IMd80SoftwareOptionsRepository md80softwareoptionsRepository;

	@Autowired
	private IApplication app;
	
	@Autowired
	private HashUtils hashUtils;

	/**
	 * Retrieves a list of Md80SoftwareOptions with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of Md80SoftwareOptions based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Md80SoftwareOptions>>> getAllMd80SoftwareOptions(int page, int rec) {
		BaseResponse<List<Md80SoftwareOptions>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all Md80SoftwareOptions Data");
			if (page == -1 && rec == -1) {
				List<Md80SoftwareOptions> md80softwareoptions = md80softwareoptionsRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, md80softwareoptions,
						(long) md80softwareoptions.size()));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<Md80SoftwareOptions> md80softwareoptionsPages = md80softwareoptionsRepository.findAll(pages);
			if (md80softwareoptionsPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						md80softwareoptionsPages.getContent(), md80softwareoptionsPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all Md80SoftwareOptions data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific Md80SoftwareOptions data by its ID.
	 *
	 * @param id The ID of the Md80SoftwareOptions to retrieve.
	 * @return A ResponseDto containing the Md80SoftwareOptions entity with the
	 *         specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<Md80SoftwareOptions>> getMd80SoftwareOptionsById(Long id) {
		BaseResponse<Md80SoftwareOptions> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching Md80SoftwareOptions Data By Id");
			Optional<Md80SoftwareOptions> md80softwareoptions = md80softwareoptionsRepository.findById(id);
			if (md80softwareoptions.isPresent()) {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, md80softwareoptions.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching Md80SoftwareOptions data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Creates new Md80SoftwareOptionss based on the provided list of DTOs.
	 *
	 * @param createmd80softwareoptionss The list of DTOs containing data for
	 *                                   creating Md80SoftwareOptions.
	 * @return A ResponseDto containing the list of created Md80SoftwareOptions
	 *         entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Md80SoftwareOptions>>> createMd80SoftwareOptions(
			List<Md80SoftwareOptionsRequestDto> md80softwareoptionssCreate) {
		BaseResponse<List<Md80SoftwareOptions>> responseObj = new BaseResponse<>();
		List<Md80SoftwareOptions> createdMd80SoftwareOptionss = new ArrayList<>();

		for (Md80SoftwareOptionsRequestDto md80softwareoptionsCreate : md80softwareoptionssCreate) {
			try {
				log.info("Creating Md80SoftwareOptions Data");
				Md80SoftwareOptions md80softwareoptions = new Md80SoftwareOptions();
				md80softwareoptions.setNavdbId(md80softwareoptionsCreate.getNavdbId());
				md80softwareoptions.setAirframeType(md80softwareoptionsCreate.getAirframeType());
				md80softwareoptions.setCreatedBy(md80softwareoptionsCreate.getCreatedBy());
				md80softwareoptions.setCreatedOn(md80softwareoptionsCreate.getCreatedOn());
				md80softwareoptions.setAcarsEnable(md80softwareoptionsCreate.getAcarsEnable());
				md80softwareoptions.setSatcomEnable(md80softwareoptionsCreate.getSatcomEnable());
				md80softwareoptions.setGpsEnable(md80softwareoptionsCreate.getGpsEnable());
				md80softwareoptions.setPredictiveRaimEnable(md80softwareoptionsCreate.getPredictiveRaimEnable());
				md80softwareoptions.setGpsFaultDetExclEnable(md80softwareoptionsCreate.getGpsFaultDetExclEnable());
				md80softwareoptions.setCreateDcrNumber(md80softwareoptionsCreate.getCreateDcrNumber());
				md80softwareoptions.setUpdateDcrNumber(md80softwareoptionsCreate.getUpdateDcrNumber());
				Md80SoftwareOptions createdMd80SoftwareOptions = md80softwareoptionsRepository
						.save(md80softwareoptions);
				createdMd80SoftwareOptionss.add(createdMd80SoftwareOptions);
			} catch (Exception ex) {
				log.error("An error occurred while creating Md80SoftwareOptions data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
			}
		}
		return responseObj
				.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdMd80SoftwareOptionss));
	}

	/**
	 * Updates existing Md80SoftwareOptionss based on the provided list of DTOs.
	 *
	 * @param md80softwareoptionssUpdate The list of DTOs containing data for
	 *                                   updating Md80SoftwareOptions.
	 * @return A ResponseDto containing the list of updated Md80SoftwareOptions
	 *         entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Md80SoftwareOptions>>> updateMd80SoftwareOptions(
			List<Md80SoftwareOptionsRequestDto> md80softwareoptionssUpdate) {
		BaseResponse<List<Md80SoftwareOptions>> responseObj = new BaseResponse<>();
		List<Md80SoftwareOptions> updatedMd80SoftwareOptionss = new ArrayList<>();

		for (Md80SoftwareOptionsRequestDto md80softwareoptionsUpdate : md80softwareoptionssUpdate) {
			try {
				log.info("Updating Md80SoftwareOptions Data");
				Md80SoftwareOptionsIdClass md80SoftwareOptionsId = new Md80SoftwareOptionsIdClass(
						md80softwareoptionsUpdate.getNavdbId(), md80softwareoptionsUpdate.getAirframeType());
				Optional<Md80SoftwareOptions> existingMd80SoftwareOptionsOptional = md80softwareoptionsRepository
						.findById(md80SoftwareOptionsId);
				if (existingMd80SoftwareOptionsOptional.isPresent()) {
					Md80SoftwareOptions existingMd80SoftwareOptions = existingMd80SoftwareOptionsOptional.get();
					existingMd80SoftwareOptions.setNavdbId(md80softwareoptionsUpdate.getNavdbId());
					existingMd80SoftwareOptions.setAirframeType(md80softwareoptionsUpdate.getAirframeType());
					existingMd80SoftwareOptions.setCreatedBy(md80softwareoptionsUpdate.getCreatedBy());
					existingMd80SoftwareOptions.setCreatedOn(md80softwareoptionsUpdate.getCreatedOn());
					existingMd80SoftwareOptions.setAcarsEnable(md80softwareoptionsUpdate.getAcarsEnable());
					existingMd80SoftwareOptions.setSatcomEnable(md80softwareoptionsUpdate.getSatcomEnable());
					existingMd80SoftwareOptions.setGpsEnable(md80softwareoptionsUpdate.getGpsEnable());
					existingMd80SoftwareOptions
							.setPredictiveRaimEnable(md80softwareoptionsUpdate.getPredictiveRaimEnable());
					existingMd80SoftwareOptions
							.setGpsFaultDetExclEnable(md80softwareoptionsUpdate.getGpsFaultDetExclEnable());
					existingMd80SoftwareOptions.setCreateDcrNumber(md80softwareoptionsUpdate.getCreateDcrNumber());
					existingMd80SoftwareOptions.setUpdateDcrNumber(md80softwareoptionsUpdate.getUpdateDcrNumber());
					Md80SoftwareOptions updatedMd80SoftwareOptions = md80softwareoptionsRepository
							.save(existingMd80SoftwareOptions);
					updatedMd80SoftwareOptionss.add(updatedMd80SoftwareOptions);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while updating Md80SoftwareOptions data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
			}
		}
		return responseObj
				.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedMd80SoftwareOptionss));
	}

	/**
	 * Deletes existing Md80SoftwareOptionss based on the provided list of DTOs.
	 *
	 * @param deletemd80softwareoptionss The list of DTOs containing data for
	 *                                   deleting Md80SoftwareOptions.
	 * @return A ResponseDto containing the list of deleted Md80SoftwareOptions
	 *         entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<Md80SoftwareOptions>>> deleteMd80SoftwareOptions(
			List<Md80SoftwareOptionsRequestDto> md80softwareoptionsDeletes) {
		BaseResponse<List<Md80SoftwareOptions>> responseObj = new BaseResponse<>();
		List<Md80SoftwareOptions> deletedMd80SoftwareOptionss = new ArrayList<>();

		for (Md80SoftwareOptionsRequestDto md80softwareoptionsDelete : md80softwareoptionsDeletes) {
			try {
				log.info("Deleting Md80SoftwareOptions Data");
				Md80SoftwareOptionsIdClass md80SoftwareOptionsId = new Md80SoftwareOptionsIdClass(
						md80softwareoptionsDelete.getNavdbId(), md80softwareoptionsDelete.getAirframeType());
				Optional<Md80SoftwareOptions> existingMd80SoftwareOptionsOptional = md80softwareoptionsRepository
						.findById(md80SoftwareOptionsId);
				if (existingMd80SoftwareOptionsOptional.isPresent()) {
					Md80SoftwareOptions existingMd80SoftwareOptions = existingMd80SoftwareOptionsOptional.get();
					md80softwareoptionsRepository.deleteById(md80SoftwareOptionsId);
					deletedMd80SoftwareOptionss.add(existingMd80SoftwareOptions);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while deleting Md80SoftwareOptions data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
			}
		}
		return responseObj
				.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedMd80SoftwareOptionss));
	}

	@Override
	public ResponseEntity<ResponseDto<List<Md80SoftwareOptions>>> searchMd80SoftwareOptions(
			Md80SoftwareOptionsQuerySearchDto md80softwareoptionsQuerySearch, int page, int rec) {
		BaseResponse<List<Md80SoftwareOptions>> responseObj = new BaseResponse<>();
		List<Md80SoftwareOptions> searchMd80SoftwareOptionss = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(md80softwareoptionsQuerySearch, "md80_software_options", "", "", true,
					page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(md80softwareoptionsQuerySearch, "md80_software_options", "", "", false,
					page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				records = app.executeQuery(searchQuery, page, rec);
			}

			for (Record searchRec : records) {
				searchMd80SoftwareOptionss.add(app.mapResultSetToClass(searchRec, Md80SoftwareOptions.class));
			}
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
					searchMd80SoftwareOptionss, total, hashUtils.encrypt(searchQuery)));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}


----------------------------------
package com.honeywell.coreptdu.datatypes.douglasapf.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.douglasapf.dto.request.DouglasApfQuerySearchDto;
import com.honeywell.coreptdu.datatypes.douglasapf.dto.request.DouglasApfRequestDto;
import com.honeywell.coreptdu.datatypes.douglasapf.entity.DouglasApf;
import com.honeywell.coreptdu.datatypes.douglasapf.entity.idclass.DouglasApfIdClass;
import com.honeywell.coreptdu.datatypes.douglasapf.repository.IDouglasApfRepository;
import com.honeywell.coreptdu.datatypes.douglasapf.service.IDouglasApfService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * DouglasApf Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class DouglasApfServiceImpl implements IDouglasApfService {

	@Autowired
	IDouglasApfRepository douglasapfRepository;

	@Autowired
	private IApplication app;
	
	@Autowired
	private HashUtils hashUtils;

	/**
	 * Retrieves a list of DouglasApf with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of DouglasApf based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<DouglasApf>>> getAllDouglasApf(int page, int rec) {
		BaseResponse<List<DouglasApf>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all DouglasApf Data");
			if (page == -1 && rec == -1) {
				List<DouglasApf> douglasapf = douglasapfRepository.findAll();
				return responseObj.render(
						responseObj.formSuccessResponse(Constants.RECORD_FETCH, douglasapf, (long) douglasapf.size()));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<DouglasApf> douglasapfPages = douglasapfRepository.findAll(pages);
			if (douglasapfPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						douglasapfPages.getContent(), douglasapfPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all DouglasApf data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific DouglasApf data by its ID.
	 *
	 * @param id The ID of the DouglasApf to retrieve.
	 * @return A ResponseDto containing the DouglasApf entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<DouglasApf>> getDouglasApfById(Long id) {
		BaseResponse<DouglasApf> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching DouglasApf Data By Id");
			Optional<DouglasApf> douglasapf = douglasapfRepository.findById(id);
			if (douglasapf.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, douglasapf.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching DouglasApf data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Creates new DouglasApfs based on the provided list of DTOs.
	 *
	 * @param createdouglasapfs The list of DTOs containing data for creating
	 *                          DouglasApf.
	 * @return A ResponseDto containing the list of created DouglasApf entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<DouglasApf>>> createDouglasApf(
			List<DouglasApfRequestDto> douglasapfsCreate) {
		BaseResponse<List<DouglasApf>> responseObj = new BaseResponse<>();
		List<DouglasApf> createdDouglasApfs = new ArrayList<>();

		for (DouglasApfRequestDto douglasapfCreate : douglasapfsCreate) {
			try {
				log.info("Creating DouglasApf Data");
				DouglasApf douglasapf = new DouglasApf();
				douglasapf.setNavdbId(douglasapfCreate.getNavdbId());
				douglasapf.setAirframeType(douglasapfCreate.getAirframeType());
				douglasapf.setRnpTakeoffValue(douglasapfCreate.getRnpTakeoffValue());
				douglasapf.setRnpTakeoffDelay(douglasapfCreate.getRnpTakeoffDelay());
				douglasapf.setRnpOceanicValue(douglasapfCreate.getRnpOceanicValue());
				douglasapf.setRnpOceanicDelay(douglasapfCreate.getRnpOceanicDelay());
				douglasapf.setRnpEnrouteValue(douglasapfCreate.getRnpEnrouteValue());
				douglasapf.setRnpEnrouteDelay(douglasapfCreate.getRnpEnrouteDelay());
				douglasapf.setRnpTerminalValue(douglasapfCreate.getRnpTerminalValue());
				douglasapf.setRnpTerminalDelay(douglasapfCreate.getRnpTerminalDelay());
				douglasapf.setRnpApproachValue(douglasapfCreate.getRnpApproachValue());
				douglasapf.setRnpApproachDelay(douglasapfCreate.getRnpApproachDelay());
				douglasapf.setUil(douglasapfCreate.getUil());
				douglasapf.setCreateDcrNumber(douglasapfCreate.getCreateDcrNumber());
				douglasapf.setUpdateDcrNumber(douglasapfCreate.getUpdateDcrNumber());
				douglasapf.setCreatedBy(douglasapfCreate.getCreatedBy());
				DouglasApf createdDouglasApf = douglasapfRepository.save(douglasapf);
				createdDouglasApfs.add(createdDouglasApf);
			} catch (Exception ex) {
				log.error("An error occurred while creating DouglasApf data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdDouglasApfs));
	}

	/**
	 * Updates existing DouglasApfs based on the provided list of DTOs.
	 *
	 * @param douglasapfsUpdate The list of DTOs containing data for updating
	 *                          DouglasApf.
	 * @return A ResponseDto containing the list of updated DouglasApf entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<DouglasApf>>> updateDouglasApf(
			List<DouglasApfRequestDto> douglasapfsUpdate) {
		BaseResponse<List<DouglasApf>> responseObj = new BaseResponse<>();
		List<DouglasApf> updatedDouglasApfs = new ArrayList<>();

		for (DouglasApfRequestDto douglasapfUpdate : douglasapfsUpdate) {
			try {
				log.info("Updating DouglasApf Data");
				DouglasApfIdClass douglasApf = new DouglasApfIdClass(douglasapfUpdate.getNavdbId(),
						douglasapfUpdate.getAirframeType());
				Optional<DouglasApf> existingDouglasApfOptional = douglasapfRepository.findById(douglasApf);
				if (existingDouglasApfOptional.isPresent()) {
					DouglasApf existingDouglasApf = existingDouglasApfOptional.get();
					existingDouglasApf.setNavdbId(douglasapfUpdate.getNavdbId());
					existingDouglasApf.setAirframeType(douglasapfUpdate.getAirframeType());
					existingDouglasApf.setRnpTakeoffValue(douglasapfUpdate.getRnpTakeoffValue());
					existingDouglasApf.setRnpTakeoffDelay(douglasapfUpdate.getRnpTakeoffDelay());
					existingDouglasApf.setRnpOceanicValue(douglasapfUpdate.getRnpOceanicValue());
					existingDouglasApf.setRnpOceanicDelay(douglasapfUpdate.getRnpOceanicDelay());
					existingDouglasApf.setRnpEnrouteValue(douglasapfUpdate.getRnpEnrouteValue());
					existingDouglasApf.setRnpEnrouteDelay(douglasapfUpdate.getRnpEnrouteDelay());
					existingDouglasApf.setRnpTerminalValue(douglasapfUpdate.getRnpTerminalValue());
					existingDouglasApf.setRnpTerminalDelay(douglasapfUpdate.getRnpTerminalDelay());
					existingDouglasApf.setRnpApproachValue(douglasapfUpdate.getRnpApproachValue());
					existingDouglasApf.setRnpApproachDelay(douglasapfUpdate.getRnpApproachDelay());
					existingDouglasApf.setUil(douglasapfUpdate.getUil());
					existingDouglasApf.setCreateDcrNumber(douglasapfUpdate.getCreateDcrNumber());
					existingDouglasApf.setUpdateDcrNumber(douglasapfUpdate.getUpdateDcrNumber());
					existingDouglasApf.setCreatedBy(douglasapfUpdate.getCreatedBy());
					DouglasApf updatedDouglasApf = douglasapfRepository.save(existingDouglasApf);
					updatedDouglasApfs.add(updatedDouglasApf);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while updating DouglasApf data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedDouglasApfs));
	}

	/**
	 * Deletes existing DouglasApfs based on the provided list of DTOs.
	 *
	 * @param deletedouglasapfs The list of DTOs containing data for deleting
	 *                          DouglasApf.
	 * @return A ResponseDto containing the list of deleted DouglasApf entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<DouglasApf>>> deleteDouglasApf(
			List<DouglasApfRequestDto> douglasapfDeletes) {
		BaseResponse<List<DouglasApf>> responseObj = new BaseResponse<>();
		List<DouglasApf> deletedDouglasApfs = new ArrayList<>();

		for (DouglasApfRequestDto douglasapfDelete : douglasapfDeletes) {
			try {
				log.info("Deleting DouglasApf Data");
				DouglasApfIdClass DouglasApfId = new DouglasApfIdClass(douglasapfDelete.getNavdbId(),
						douglasapfDelete.getAirframeType());
				Optional<DouglasApf> existingDouglasApfOptional = douglasapfRepository.findById(DouglasApfId);
				if (existingDouglasApfOptional.isPresent()) {
					DouglasApf existingDouglasApf = existingDouglasApfOptional.get();
					douglasapfRepository.deleteById(DouglasApfId);
					deletedDouglasApfs.add(existingDouglasApf);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while deleting DouglasApf data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedDouglasApfs));
	}

	@Override
	public ResponseEntity<ResponseDto<List<DouglasApf>>> searchDouglasApf(
			DouglasApfQuerySearchDto douglasapfQuerySearch, int page, int rec) {
		BaseResponse<List<DouglasApf>> responseObj = new BaseResponse<>();
		List<DouglasApf> searchDouglasApfs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(douglasapfQuerySearch, "douglas_apf", "", "", true,
					page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(douglasapfQuerySearch, "douglas_apf", "", "", false,
					page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				records = app.executeQuery(searchQuery, page, rec);
			}

			for (Record searchRec : records) {
				searchDouglasApfs.add(app.mapResultSetToClass(searchRec, DouglasApf.class));
			}
			for (DouglasApf douglasApf : searchDouglasApfs) {
				douglasApf.setQueryDataSourceName("DOUGLAS_APF");
				douglasApf.setRecordStatus("QUERIED");
			}
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchDouglasApfs, total,
					hashUtils.encrypt(searchQuery)));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}
