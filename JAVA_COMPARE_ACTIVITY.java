package com.honeywell.coreptdu.datatypes.compareactivity.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Struct;
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

import com.honeywell.coreptdu.datatypes.compareactivity.block.CompareActArpt;
import com.honeywell.coreptdu.datatypes.compareactivity.block.CompareActCust;
import com.honeywell.coreptdu.datatypes.compareactivity.block.CompareActCycl;
import com.honeywell.coreptdu.datatypes.compareactivity.block.CompareActSupp;
import com.honeywell.coreptdu.datatypes.compareactivity.block.CompareRecordCountBlk;
import com.honeywell.coreptdu.datatypes.compareactivity.block.Ssa;
import com.honeywell.coreptdu.datatypes.compareactivity.block.SsaSupp;
import com.honeywell.coreptdu.datatypes.compareactivity.block.StdRecordCountBlk;
import com.honeywell.coreptdu.datatypes.compareactivity.block.TldStdAirports;
import com.honeywell.coreptdu.datatypes.compareactivity.block.Webutil;
import com.honeywell.coreptdu.datatypes.compareactivity.dto.request.CompareActivityTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.compareactivity.dto.response.CompareActivityTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.compareactivity.service.ICompareActivityTriggerService;
import com.honeywell.coreptdu.datatypes.coreptmenummb.serviceimpl.CoreptMenuMmbServiceImpl;
import com.honeywell.coreptdu.datatypes.exportdestination.block.SelectOptions;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.request.ExportDestinationTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.response.ExportDestinationTriggerResponseDto;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.exception.FormTriggerFailureException;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.Global;
import com.honeywell.coreptdu.global.dto.Parameter;
import com.honeywell.coreptdu.global.dto.ReportDetail;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.global.dto.SystemDto;
import com.honeywell.coreptdu.global.forms.BlockDetail;
import com.honeywell.coreptdu.global.forms.Event;
import com.honeywell.coreptdu.global.forms.FormConstant;
import com.honeywell.coreptdu.pkg.body.DisplayAlert;
import com.honeywell.coreptdu.pll.CoreptLib;
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

import ch.qos.logback.core.spi.ErrorCodes;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.internal.OracleTypes;

@Slf4j
@Service
@RequestScope
public class CompareActivityTriggerServiceImpl extends GenericTemplateForm<CompareActivityTriggerServiceImpl>
		implements ICompareActivityTriggerService {

	@Getter
	@Setter
	private CompareActCycl compareActCycl = new CompareActCycl();
	@Getter
	@Setter
	private Block<Ssa> ssa = new Block<>();
	@Getter
	@Setter
	private Webutil webutil = new Webutil();
	@Getter
	@Setter
	private Block<SsaSupp> ssaSupp = new Block<>();
	@Getter
	@Setter
	private Block<CompareRecordCountBlk> compareRecordCountBlk = new Block<>();
	@Getter
	@Setter
	private TldStdAirports tldStdAirports = new TldStdAirports();
	@Getter
	@Setter
	private CompareActSupp compareActSupp = new CompareActSupp();
	@Getter
	@Setter
	private Block<CompareActArpt> compareActArpt = new Block<>();
	@Getter
	@Setter
	private StdRecordCountBlk stdRecordCountBlk = new StdRecordCountBlk();
	@Getter
	@Setter
	private Block<CompareActCust> compareActCust = new Block<>();
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
	private List<Event> event = new ArrayList<>();
	@Getter
	@Setter
	private HashMap<String, RecordGroup> groups = new HashMap<>();
	@Autowired
	private IApplication app;
	@Autowired
	private CoreptLib coreptLib;
	@Autowired
	private GenericNativeQueryHelper genericNativeQueryHelper;
	@Getter
	@Setter
	private List<String> blocksOrder = new ArrayList<>();
	@Getter
	@Setter
	private SelectOptions selectOptions = new SelectOptions();
	@Autowired
	private CoreptMenuMmbServiceImpl coreptMenuMmbServiceImpl;

	@Autowired
	private DisplayAlert displayAlert;

	@Override
	public void populateProcessingCycle(String piBlock, String piItem) throws Exception {
		log.info("populateProcessingCycle Executing");
		try {
			RecordGroup lrgRecordGrp = null;
			Integer lnPopulateGrp = 0;

			lrgRecordGrp = findGroup("focal");
			if (lrgRecordGrp != null) {
				deleteGroup(groups, "lrgRecordGrp");
			}
			lrgRecordGrp = createGroupFromQuery("focal",
					"SELECT TO_CHAR(processing_cycle) AS processing_cycle, TO_CHAR(processing_cycle)  AS processing_cycle_1 FROM pl_std_airport GROUP BY processing_cycle  ORDER BY 1 DESC ");
			lnPopulateGrp = populateGroup(lrgRecordGrp);
			if (!Objects.equals(lnPopulateGrp, 0)) {
				message("populate group had error " + toString(lnPopulateGrp));
			}

			populateList(piBlock + "." + piItem, lrgRecordGrp);

			log.info("populateProcessingCycle Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing populateProcessingCycle" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void populateDataSupp(String piBlock, String piItem) throws Exception {
		log.info("populateDataSupp Executing");
		try {
			RecordGroup lrgRecordGrp = null;
			Integer lnPopulateGrp = 0;

			lrgRecordGrp = findGroup("focal");
			if (lrgRecordGrp != null) {
				deleteGroup(groups, "lrgRecordGrp");

			}

			lrgRecordGrp = createGroupFromQuery("focal",
					"SELECT data_supplier_name, data_supplier FROM data_supplier ORDER BY 1 ");

			lnPopulateGrp = populateGroup(lrgRecordGrp);
			if (!Objects.equals(lnPopulateGrp, 0)) {
				message("populate group had error " + toString(lnPopulateGrp));
			}

			populateList(piBlock + "." + piItem, lrgRecordGrp);

			log.info("populateDataSupp Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing populateDataSupp" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void unsetMenuItemsPrc() throws Exception {
		log.info("unsetMenuItemsPrc Executing");

		try {

			setMenuItemProperty(rtrim("action") + "." + ltrim("save"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("edit") + "." + ltrim("edit"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("edit") + "." + ltrim("cut"), FormConstant.ENABLED, FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("edit") + "." + ltrim("copy"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("edit") + "." + ltrim("paste"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("edit") + "." + ltrim("list"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("query") + "." + ltrim("enter"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("query") + "." + ltrim("execute"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("query") + "." + ltrim("countHits"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("insert"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("remove"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("duplicate"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("record") + "." + ltrim("clear"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("field") + "." + ltrim("clear"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("block") + "." + ltrim("clear"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("field") + "." + ltrim("duplicate"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("tools") + "." + ltrim("duplicate"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("tools") + "." + ltrim("exportData"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("help") + "." + ltrim("keys"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("help") + "." + ltrim("displayError"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("query") + "." + ltrim("fetch.Next.Set"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);
			setMenuItemProperty(rtrim("tools") + "." + ltrim("add.Query.Conditions"), FormConstant.ENABLED,
					FormConstant.BPROPERTY_FALSE);

			log.info("unsetMenuItemsPrc Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing unsetMenuItemsPrc" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> whenNewFormInstance(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" whenNewFormInstance Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			unsetMenuItemsPrc();
			setWindowProperty("", "title", "Compare Activity");

			setWindowProperty(FORMS_MDI_WINDOW, WINDOW_STATE, MAXIMIZE);

			populateDataSupp("compareActCycl", "dataSupplier");

			populateProcessingCycle("compareActCycl", "currCycle");

			populateProcessingCycle("compareActCycl", "prevCycle");
			goItem("compareActCycl.dataSupplier");
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> whenTabPageChanged(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" whenTabPageChanged Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(system.getTabNewPage(), "acrossSupp")) {

				populateDataSupp("compareActSupp", "formSupplier");

				populateDataSupp("compareActSupp", "toSupplier");

				populateProcessingCycle("compareActSupp", "processingCycle");
				goItem("compareActSupp.processingCycle");

			}

			else if (Objects.equals(system.getTabNewPage(), "arptCycl")) {

				populateDataSupp("tldStdAirports", "dataSupplier");

				populateProcessingCycle("tldStdAirports", "processingCycle");
				goItem("tldStdAirports.processingCycle");

			}

			else if (Objects.equals(system.getTabNewPage(), "acrossCycl")) {

				populateProcessingCycle("compareActCycl", "currCycle");

				populateProcessingCycle("compareActCycl", "prevCycle");

				populateDataSupp("compareActCycl", "dataSupplier");
				goItem("compareActCycl.dataSupplier");

			}

			else if (Objects.equals(system.getTabNewPage(), "recordsCountTab")) {

				populateDataSupp("stdRecordCountBlk", "dataSupplier");

				populateProcessingCycle("stdRecordCountBlk", "processingCycle");
				goItem("stdRecordCountBlk.processingCycle");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenTabPageChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenTabPageChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActCyclArptRangeWhenNewItemInstance(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" compareActCyclArptRangeWhenNewItemInstance Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(compareActCycl.getDataSupplier(), null)
					&& !Objects.equals(compareActCycl.getCurrCycle(), null)
					&& !Objects.equals(compareActCycl.getPrevCycle(), null)
					&& (Objects.equals(compareActCycl.getAirportIdent(), "")
							|| Objects.equals(compareActCycl.getAirportIdent(), null))) {
				coreptLib.dspMsg("Enter Airport Ident to Filter.");
				goItem("compareActCycl.airportIdent");

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActCyclArptRangeWhenNewItemInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActCyclArptRangeWhenNewItemInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActCyclGoWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto, int page, int rec) throws Exception {
		log.info(" compareActCyclGoWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			int start = 0;
			int end = 0;

			Integer lnCount = 0;
			String lsPrevAirportIdent = "$$$$";
			String lsSsa = "";
			Object[] currentBatch = null;

			if (!Objects.equals(compareActCycl.getSearch(), "true") &&
					Objects.equals(compareActCycl.getAirportIdent(), null) &&
					!Objects.equals(compareActCycl.getSearchAirportIdent(), null)) {
				for (Integer index = ssa.getData().size(); index >= 0; index--) {
					if (!Objects.equals(ssa.getRow(index).getAirportIdent(), null)) {
						lsPrevAirportIdent = ssa.getRow(index).getAirportIdent();
						break;
					}
				}
			}

			ssa.getData().clear();

			if (OracleHelpers.isNullorEmpty(compareActCycl.getDataSupplier())) {
				coreptLib.dspMsg("Select Data Supplier.");
				goItem("compareActCycl.dataSupplier");
				throw new FormTriggerFailureException(event);
			}

			if (OracleHelpers.isNullorEmpty(compareActCycl.getCurrCycle())) {
				coreptLib.dspMsg("Select Current Cycle.");
				goItem("compareActCycl.currCycle");
				throw new FormTriggerFailureException();
			}

			if (OracleHelpers.isNullorEmpty(compareActCycl.getPrevCycle())) {
				coreptLib.dspMsg("Select Previous Cycle.");
				goItem("compareActCycl.prevCycle");
				throw new FormTriggerFailureException();
			}

			if (Objects.equals(compareActCycl.getSid(), "D")) {
				lsSsa = lsSsa + compareActCycl.getSid();
			}

			if (Objects.equals(compareActCycl.getStar(), "E")) {
				lsSsa = lsSsa + compareActCycl.getStar();
			}

			if (Objects.equals(compareActCycl.getApproach(), "F")) {
				lsSsa = lsSsa + compareActCycl.getApproach();
			}

			// goBlock("ssa", "airportIdent");
			goBlock("ssa", "");
			// clearBlock("ssa", "noCommit");

			Map<String, Object> compareActCycleDbproc = app.executeProcedure("CPTS", "Compare_Activity_Report",
					"forms_utilities", new ProcedureInParameter("pi_Ssa", lsSsa, OracleTypes.VARCHAR),
					new ProcedureInParameter("pi_From_Data_Supplier", compareActCycl.getDataSupplier(),
							OracleTypes.VARCHAR),
					new ProcedureInParameter("pi_Curr_Cycle", compareActCycl.getCurrCycle(), OracleTypes.NUMBER),
					new ProcedureInParameter("pi_Prev_Cycle", compareActCycl.getPrevCycle(), OracleTypes.NUMBER),
					new ProcedureInParameter("pi_Only_DEL_ADD", compareActCycl.getDelAdd(), OracleTypes.VARCHAR),
					new ProcedureOutParameter("po_Count", OracleTypes.NUMBER),
					new ProcedureOutParameter("po_ltab_Compare_Activity_Table", OracleTypes.ARRAY,
							"FORMS_UTILITIES.COMPARE_ACTIVITY_TABLE"),
					new ProcedureInParameter("pi_Airport_Ident", compareActCycl.getAirportIdent(), OracleTypes.VARCHAR),
					new ProcedureInParameter("pi_Arpt_Range", compareActCycl.getArptRange(), OracleTypes.NUMBER),
					new ProcedureInParameter("pi_To_Data_Supplier", null, OracleTypes.VARCHAR));

			lnCount = toInteger(compareActCycleDbproc.get("po_Count"));

			Array arrayListLtabComActTab = (Array) compareActCycleDbproc.get("po_ltab_Compare_Activity_Table");
			Object[] arrayToObj = (Object[]) arrayListLtabComActTab.getArray();

			start = page;
			end = Math.min(start + rec, lnCount);
			String revPrevAirportIdent = "$$$$";

			// ----------------------------Search
			// Logic----------------------------------------------------------------------
			int recCount = 0;
			int endIndex = 0;
			if (page == 0 && rec == 0) {
				currentBatch = Arrays.copyOfRange(arrayToObj, start, lnCount);

				int i = 0;
				for (Object strcut : currentBatch) {
					if (strcut != null) {
						Struct structs = (Struct) strcut;
						Object[] ltabCompareActivityTable = structs.getAttributes();
						if (ltabCompareActivityTable[1].equals(compareActCycl.getSearchAirportIdent())) {
							endIndex = i;
							recCount++;
							compareActCycl.setSearchIndex(recCount);
						}
					}
					i++;
				}
				start = endIndex - recCount + 1;
				if (recCount > 0) {

					currentBatch = Arrays.copyOfRange(arrayToObj, start, endIndex + 1);
				} else {
					OracleHelpers.ResponseMapper(this, resDto);
					return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
				}
			} else {
				currentBatch = Arrays.copyOfRange(arrayToObj, start, end);
			}
			// --------------------------------------------------------------------------------------------------------------
			if (!Objects.equals(page, 0)) {
				Object[] revObjects = Arrays.copyOfRange(arrayToObj, start - 1, start);

				for (Object strcut : revObjects) {
					if (strcut != null) {
						Struct structs = (Struct) strcut;
						Object[] ltabCompareActivityTable = structs.getAttributes();
						revPrevAirportIdent = (String) ltabCompareActivityTable[1];
					}
				}
				revObjects = null;
			}

			arrayListLtabComActTab = null;
			arrayToObj = null;
			int i = 0;
			int j = start;
			compareActCycl.setTotalCnt(lnCount);
			if (lnCount > 0) {
				goBlock("ssa", "");
				for (Object strcut : currentBatch) {
					if (strcut != null) {
						Struct structs = (Struct) strcut;
						Object[] ltabCompareActivityTable = structs.getAttributes();

						ssa.add(new Ssa());

						if (!Arrays.asList(lsPrevAirportIdent, revPrevAirportIdent)
								.contains(ltabCompareActivityTable[1])) {
							ssa.getRow(i).setAirportIdent(toString(ltabCompareActivityTable[1]));
						}

						// if (!Objects.equals(lsPrevAirportIdent, ltabCompareActivityTable[1])) {
						// ssa.getRow(i).setAirportIdent(toString(ltabCompareActivityTable[1]));
						// }
						ssa.getRow(i).setRecordIndex(j);
						ssa.getRow(i).setApproachIdent(toString(ltabCompareActivityTable[2]));
						ssa.getRow(i).setProcType(toString(ltabCompareActivityTable[6]));
						if (Objects.equals(ltabCompareActivityTable[4], "ON_ADD")) {
							ssa.getRow(i).setActivity("ADD");
							setItemInstanceProperty("ssa.airport_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa.approach_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa.proc_type", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa.activity", toString(j), "visual_attribute", "");
						} else if (Objects.equals(ltabCompareActivityTable[4], "ON_DEL")) {
							ssa.getRow(i).setActivity("DEL");
							setItemInstanceProperty("ssa.airport_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa.approach_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa.proc_type", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa.activity", toString(j), "visual_attribute", "");
						} else if (Objects.equals(ltabCompareActivityTable[4], "APT_ADD")) {

							ssa.getRow(i).setActivity(toString(ltabCompareActivityTable[4]));
							setItemInstanceProperty("ssa.airport_ident", toString(j), "visual_attribute", "aptAdd");
							setItemInstanceProperty("ssa.approach_ident", toString(j), "visual_attribute", "aptAdd");
							setItemInstanceProperty("ssa.proc_type", toString(j), "visual_attribute", "aptAdd");
							setItemInstanceProperty("ssa.activity", toString(j), "visual_attribute", "aptAdd");
						} else if (Objects.equals(ltabCompareActivityTable[4], "APT_DEL")) {

							ssa.getRow(i).setActivity(toString(ltabCompareActivityTable[4]));
							setItemInstanceProperty("ssa.airport_ident", toString(j), "visual_attribute", "aptDel");
							setItemInstanceProperty("ssa.approach_ident", toString(j), "visual_attribute", "aptDel");
							setItemInstanceProperty("ssa.proc_type", toString(j), "visual_attribute", "aptDel");
							setItemInstanceProperty("ssa.activity", toString(j), "visual_attribute", "aptDel");

						}
						lsPrevAirportIdent = toString(ltabCompareActivityTable[1]);
						nextRecord("");

						i++;
						j++;

					}
					firstRecord(" ");
					revPrevAirportIdent = "$$$$";

				}

			} else {
				coreptLib.dspMsg("None of the Procedures are available.");
				goBlock("compareActCycl", "dataSupplier");
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActCyclGoWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActCyclGoWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActCyclClearWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" compareActCyclClearWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			goBlock("ssa", "");
			clearBlock("ssa", "noCommit");

			goBlock("compareActCycl", "");
			clearBlock("compareActCycl", "");
			compareActCycl.setDelAdd("N");
			ssa.getData().clear();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActCyclClearWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActCyclClearWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActCyclSearchWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" compareActCyclSearchWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(ssa.getRow(system.getCursorRecordIndex()).getApproachIdent(), null)) {
				coreptLib.dspMsg("Perform Compare Activity before searching an Airport.");
				throw new FormTriggerFailureException(event);
			}

			if (!Objects.equals(compareActCycl.getSearchAirportIdent(), null)) {

				// 0goBlock("ssa", "");
				compareActCycl.setSearch("true");
				compareActCyclGoWhenButtonPressed(reqDto, 0, 0);
				compareActCycl.setSearch(null);
				compareActCycl.setTotalCnt(compareActCycl.getSearchIndex());
				// for (Ssa ssasrh : ssa.getData()) {
				// if (ssasrh.getAirportIdent() != null) {
				// if (ssasrh.getAirportIdent().equals(compareActCycl.getSearchAirportIdent()))
				// {
				// compareActCycl.setSearchIndex(i);
				// break;
				// }
				// }
				// i++;
				// }

				// if
				// (!Objects.equals(nvl(ssaSupp.getRow(system.getCursorRecordIndex()).getAirportIdent(),
				// "$$$"),
				// compareActSupp.getSearchAirportIdent()))
				if (compareActCycl.getSearchIndex() == null) {
					coreptLib.dspMsg("Airport not found.");
					firstRecord(null);
					// goItem("compareActCycl.search_airport_ident");
					global.setErrorCode(400);
					goBlock("compareActCycl", "search_airport_ident");
				}

			} else {
				// coreptLib.dspMsg("Enter Airport to Find.");
				// goItem("compareActCycl.searchAirportIdent");
				// global.setErrorCode(400);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActCyclSearchWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActCyclSearchWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActSuppArptRangeWhenNewItemInstance(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" compareActSuppArptRangeWhenNewItemInstance Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(compareActSupp.getProcessingCycle(), null)
					&& !Objects.equals(compareActSupp.getFromSupplier(), null)
					&& !Objects.equals(compareActSupp.getToSupplier(), null)
					&& (Objects.equals(compareActSupp.getAirportIdent(), "")
							|| Objects.equals(compareActSupp.getAirportIdent(), null))) {
				coreptLib.dspMsg("Enter Airport Ident to Filter.");
				goItem("compareActSupp.airportIdent");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActSuppArptRangeWhenNewItemInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActSuppArptRangeWhenNewItemInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActSuppGoWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto, int page, int rec) throws Exception {
		log.info(" compareActSuppGoWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// Object ltabCompareActivityTable = null;
			Integer lnCount = 0;
			String lsPrevAirportIdent = "$$$$";
			String lsSsa = "";
			Object[] currentBatuch;
			if (!Objects.equals(compareActSupp.getSearch(), "true") &&
					Objects.equals(compareActSupp.getAirportIdent(), null) &&
					!Objects.equals(compareActSupp.getSearchAirportIdent(), null)) {
				for (Integer index = ssaSupp.getData().size(); index >= 0; index--) {
					if (!Objects.equals(ssaSupp.getRow(index).getAirportIdent(), null)) {
						lsPrevAirportIdent = ssaSupp.getRow(index).getAirportIdent();
						break;
					}
				}
			}
			ssaSupp.getData().clear();

			if (Objects.equals(compareActSupp.getProcessingCycle(), null)
					|| Objects.equals(compareActSupp.getProcessingCycle(), "")) {
				coreptLib.dspMsg("Select Processing Cycle.");
				goItem("compareActCycl.processing_cycle");
				throw new FormTriggerFailureException(event);
			}

			if (Objects.equals(compareActSupp.getFromSupplier(), null)
					|| Objects.equals(compareActSupp.getFromSupplier(), "")) {
				coreptLib.dspMsg("Select From Supplier.");
				goItem("compareActCycl.from_supplier");
				throw new FormTriggerFailureException(event);
			}

			if (Objects.equals(compareActSupp.getToSupplier(), null)
					|| Objects.equals(compareActSupp.getToSupplier(), "")) {
				coreptLib.dspMsg("Select To Supplier.");
				goItem("compareActCycl.to_supplier");
				throw new FormTriggerFailureException();
			}

			if (Objects.equals(compareActSupp.getSid(), "D")) {
				lsSsa = lsSsa + compareActSupp.getSid();

			}

			if (Objects.equals(compareActSupp.getStar(), "E")) {
				lsSsa = lsSsa + compareActSupp.getStar();

			}

			if (Objects.equals(compareActSupp.getApproach(), "F")) {
				lsSsa = lsSsa + compareActSupp.getApproach();

			}

			goBlock("ssaSupp", "");
			// clearBlock("ssaSupp", "noCommit");

			Map<String, Object> compareActSuppDbproc = app.executeProcedure("CPTS", "Compare_Activity_Report",
					"forms_utilities", new ProcedureInParameter("pi_Ssa", lsSsa, OracleTypes.VARCHAR),
					new ProcedureInParameter("pi_From_Data_Supplier", compareActSupp.getFromSupplier(),
							OracleTypes.VARCHAR),
					new ProcedureInParameter("pi_Curr_Cycle", compareActSupp.getProcessingCycle(), OracleTypes.NUMBER),
					new ProcedureInParameter("pi_Prev_Cycle", null, OracleTypes.NUMBER),
					new ProcedureInParameter("pi_Only_DEL_ADD", compareActSupp.getDelAdd(), OracleTypes.VARCHAR),
					new ProcedureOutParameter("po_Count", OracleTypes.NUMBER),
					new ProcedureOutParameter("po_ltab_Compare_Activity_Table", OracleTypes.ARRAY,
							"FORMS_UTILITIES.COMPARE_ACTIVITY_TABLE"),
					new ProcedureInParameter("pi_Airport_Ident", compareActSupp.getAirportIdent(), OracleTypes.VARCHAR),
					new ProcedureInParameter("pi_Arpt_Range", compareActSupp.getArptRange(), OracleTypes.NUMBER),
					new ProcedureInParameter("pi_To_Data_Supplier", compareActSupp.getToSupplier(),
							OracleTypes.VARCHAR));

			lnCount = toInteger(compareActSuppDbproc.get("po_Count"));
			int start = page;
			int end = Math.min(start + rec, lnCount);
			compareActSupp.setTotalCnt(lnCount);
			Array arrayListLtabComActTab = (Array) compareActSuppDbproc.get("po_ltab_Compare_Activity_Table");
			Object[] arrayToObj = (Object[]) arrayListLtabComActTab.getArray();
			// Object[] currentBatuch = Arrays.copyOfRange(arrayToObj, start, end);
			// ----------------------------Search
			// Logic----------------------------------------------------------------------
			int recCount = 0;
			int endIndex = 0;
			if (page == 0 && rec == 0) {
				currentBatuch = Arrays.copyOfRange(arrayToObj, start, lnCount);

				int i = 0;
				for (Object strcut : currentBatuch) {
					if (strcut != null) {
						Struct structs = (Struct) strcut;
						Object[] ltabCompareActivityTable = structs.getAttributes();
						if (ltabCompareActivityTable[1].equals(compareActSupp.getSearchAirportIdent())) {
							endIndex = i;
							recCount++;
							compareActSupp.setSearchIndex(recCount);
						}
					}
					i++;
				}
				start = endIndex - recCount + 1;
				if (recCount > 0) {

					currentBatuch = Arrays.copyOfRange(arrayToObj, start, endIndex + 1);
				} else {
					OracleHelpers.ResponseMapper(this, resDto);
					return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
				}
			}
			// --------------------------------------------------------------------------------------------------------------
			else {
				currentBatuch = Arrays.copyOfRange(arrayToObj, start, end);
			}
			String revPrevAirportIdent = "$$$$";

			if (!Objects.equals(page, 0)) {
				Object[] revObjects = Arrays.copyOfRange(arrayToObj, start - 1, start);

				for (Object strcut : revObjects) {
					if (strcut != null) {
						Struct structs = (Struct) strcut;
						Object[] ltabCompareActivityTable = structs.getAttributes();
						revPrevAirportIdent = (String) ltabCompareActivityTable[1];
					}
				}
			}
			arrayListLtabComActTab = null;
			arrayToObj = null;
			int i = 0;
			int j = page;
			System.err.println(j + " Out side of the loop" + i);
			if (lnCount > 0) {
				goBlock("ssaSupp", "");
				for (Object strcut : currentBatuch) {
					if (strcut != null) {
						Struct structs = (Struct) strcut;
						Object[] ltabCompareActivityTable = structs.getAttributes();
						ssaSupp.add(new SsaSupp());
						System.err.println(j + "  IN side of the loop" + i);
						ssaSupp.getRow(i).setRecordIndex(j);
						ssaSupp.getRow(i).setApproachIdent(toString(ltabCompareActivityTable[2]));
						ssaSupp.getRow(i).setProcType(toString(ltabCompareActivityTable[6]));

						// if (!Objects.equals(lsPrevAirportIdent, ltabCompareActivityTable[1])) {
						// ssaSupp.getRow(i).setAirportIdent(toString(ltabCompareActivityTable[1]));
						// }

						if (!Arrays.asList(lsPrevAirportIdent, revPrevAirportIdent)
								.contains(ltabCompareActivityTable[1])) {
							ssaSupp.getRow(i).setAirportIdent(toString(ltabCompareActivityTable[1]));
						}
						if (Objects.equals(ltabCompareActivityTable[4], "ON_ADD")) {
							ssaSupp.getRow(i).setActivity("ADD_IN_" + compareActSupp.getToSupplier());
							setItemInstanceProperty("ssa_supp.airport_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa_supp.approach_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa_supp.proc_type", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa_supp.activity", toString(j), "visual_attribute", "");
						} else if (Objects.equals(ltabCompareActivityTable[4], "ON_DEL")) {
							ssaSupp.getRow(i).setActivity("DEL_IN_" + compareActSupp.getFromSupplier());
							setItemInstanceProperty("ssa_supp.airport_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa_supp.approach_ident", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa_supp.proc_type", toString(j), "visual_attribute", "");
							setItemInstanceProperty("ssa_supp.activity", toString(j), "visual_attribute", "");
						} else if (Pattern.compile("APT.{1}ADD").matcher(toString(ltabCompareActivityTable[4]))
								.matches()) {
							ssaSupp.getRow(i).setActivity("APT_ADD_IN_" + compareActSupp.getToSupplier());
							setItemInstanceProperty("ssa_supp.airport_ident", toString(j), "visual_attribute",
									"aptAdd");
							setItemInstanceProperty("ssa_supp.approach_ident", toString(j), "visual_attribute",
									"aptAdd");
							setItemInstanceProperty("ssa_supp.proc_type", toString(j), "visual_attribute", "aptAdd");
							setItemInstanceProperty("ssa_supp.activity", toString(j), "visual_attribute", "aptAdd");

						} else if (Pattern.compile("APT.{1}DEL").matcher(toString(ltabCompareActivityTable[4]))
								.matches()) {
							ssaSupp.getRow(i).setActivity("APT_DEL_IN_" + compareActSupp.getFromSupplier());
							setItemInstanceProperty("ssa_supp.airport_ident", toString(j), "visual_attribute",
									"aptDel");
							setItemInstanceProperty("ssa_supp.approach_ident", toString(j), "visual_attribute",
									"aptDel");
							setItemInstanceProperty("ssa_supp.proc_type", toString(j), "visual_attribute", "aptDel");
							setItemInstanceProperty("ssa_supp.activity", toString(j), "visual_attribute", "aptDel");

						}
						lsPrevAirportIdent = toString(ltabCompareActivityTable[1]);
						nextRecord("");
						i++;
						j++;
					}
					firstRecord("");
				}
			} else {
				coreptLib.dspMsg("None of the Procedures are available.");
				goBlock("compareActSupp", "processingCycle");
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActSuppGoWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActSuppGoWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActSuppClearWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" compareActSuppClearWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goBlock("ssaSupp", "");
			clearBlock("ssaSupp", "noCommit");
			goBlock("compareActSupp", "");
			clearBlock("compareActSupp", "noCommit");
			compareActSupp.setDelAdd("N");
			ssaSupp.getData().clear();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActSuppClearWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActSuppClearWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> compareActSuppSearchWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" compareActSuppSearchWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(ssaSupp.getRow(system.getCursorRecordIndex()).getApproachIdent(), null)) {
				coreptLib.dspMsg("Perform Compare Activity before searching an Airport.");
				throw new FormTriggerFailureException(event);
			}
			int i = 0;
			if (!Objects.equals(compareActSupp.getSearchAirportIdent(), null)) {

				goBlock("ssaSupp", "");
				compareActSupp.setSearch("true");
				compareActSuppGoWhenButtonPressed(reqDto, 0, 0);
				compareActSupp.setSearch(null);
				compareActSupp.setTotalCnt(compareActSupp.getSearchIndex());
				// for (SsaSupp ssaSuppsrh : ssaSupp.getData()) {
				// if (ssaSuppsrh.getAirportIdent() != null) {
				// if
				// (ssaSuppsrh.getAirportIdent().equals(compareActSupp.getSearchAirportIdent()))
				// {
				// compareActSupp.setSearchIndex(i);
				// break;
				// }
				// }
				// i++;
				// }

				// if
				// (!Objects.equals(nvl(ssaSupp.getRow(system.getCursorRecordIndex()).getAirportIdent(),
				// "$$$"),
				// compareActSupp.getSearchAirportIdent()))
				if (compareActSupp.getSearchIndex() == null) {
					coreptLib.dspMsg("Airport not found.");
					firstRecord(null);
					// goItem("compareActSupp.search_airport_ident");
					global.setErrorCode(400);
					goBlock("compareActSupp", "search_airport_ident");
				}

			}

			else {
				// coreptLib.dspMsg("Enter Airport to Find.");
				// goItem("compareActSupp.search_airport_ident");
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" compareActSuppSearchWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the compareActSuppSearchWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> tldStdAirportsFindAirportsWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStdAirportsFindAirportsWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// TODO TYPEType_Ref_CurISREFCURSOR;
			String tldArptCur = null;
			List<Record> records = null;
			goBlock("compareActCust", "");
			Integer recCount = 0;
			compareActArpt.getData().clear();

			clearBlock("compareActCust", "noCommit");
			tldStdAirports.setTotalCustomers(0);
			if (Objects.equals(tldStdAirports.getProcessingCycle(), null)
					|| Objects.equals(tldStdAirports.getProcessingCycle(), "")) {

				coreptLib.dspMsg("Select Processing Cycle.");
				goItem("tldStdAirports.processingCycle");
				throw new FormTriggerFailureException(event);

			}

			if (Objects.equals(tldStdAirports.getDataSupplier(), null)
					|| Objects.equals(tldStdAirports.getDataSupplier(), "")) {

				coreptLib.dspMsg("Select Data Supplier.");
				goItem("tld_std_airports.data_supplier");
				throw new FormTriggerFailureException(event);

			}

			goBlock("compareActArpt", "");
			clearBlock("compareActArpt", "noCommit");

			tldArptCur = "SELECT tld_apt.airport_ident tld_arpt" + "		     FROM pl_tld_airport tld_apt, "
					+ "		     	    pl_std_airport std_apt"
					+ "		    WHERE tld_apt.data_supplier     = 	std_apt.data_supplier"
					+ "		      AND tld_apt.processing_cycle  = 	std_apt.processing_cycle"
					+ "		      AND tld_apt.airport_ident 		= 	std_apt.airport_ident"
					+ "		      AND tld_apt.processing_cycle 	= 	?"
					+ "		      AND tld_apt.data_supplier 		= 	?"
					+ "		      AND tld_apt.airport_ident 	LIKE 	'' || NVL(?,'%') || ''"
					+ "	   GROUP BY tld_apt.airport_ident" + "	   ORDER BY 1";
			records = app.executeQuery(tldArptCur, tldStdAirports.getProcessingCycle(),
					tldStdAirports.getDataSupplier(), tldStdAirports.getAirportIdent());

			if (records == null || records.isEmpty()) {
				tldStdAirports.setTotalAirports(0);
				coreptLib.dspMsg("None of the Tailored Airport(s) is/are available as \nStandard Airport(s).");
				goItem("compareActArpt.airportIdent");
				goBlock("tldStdAirports", " ");
				OracleHelpers.ResponseMapper(this, resDto);
				throw new FormTriggerFailureException();
			} else {
				for (Record record : records) {
					CompareActArpt compareActArpts = new CompareActArpt();
					compareActArpts.setAirportIdent(record.getString());
					compareActArpt.add(compareActArpts);
					recCount++;
				}
				compareActCust.getData().clear();
				tldStdAirports.setTotalAirports(recCount);
			}
			firstRecord("");

			for (int i = 0; i < event.size(); i++) {
				Event eve = event.get(i);
				if (eve.getName().equals("setAlertProperty")) {
					Event moveto = event.remove(i);
					event.add(event.size(), moveto);
					i--;

				}
				if (eve.getName().equals("showAlert")) {
					Event moveto = event.remove(i);
					event.add(moveto);
					break;
				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStdAirportsFindAirportsWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStdAirportsFindAirportsWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> tldStdAirportsFindCustWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStdAirportsFindCustWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// TODO TYPEType_Ref_CurISREFCURSOR;
			String tldArptCur = null;
			List<Record> records = null;
			// CompareActCust airportsFindCust = new CompareActCust();
			goBlock("compareActArpt", "");
			compareActCust.getData().clear();
			Integer recCount = 0;
			// TODO CLEAR_BLOCK(NO_COMMIT);
			clearBlock("compareActArpt", "noCommit");
			tldStdAirports.setTotalAirports(0);
			if (Objects.equals(tldStdAirports.getProcessingCycle(), null)
					|| Objects.equals(tldStdAirports.getProcessingCycle(), "")) {
				coreptLib.dspMsg("Select Processing Cycle.");
				goItem("tld_std_airports.processing_cycle");
				throw new FormTriggerFailureException(event);

			}
			if (Objects.equals(tldStdAirports.getDataSupplier(), null)
					|| Objects.equals(tldStdAirports.getDataSupplier(), "")) {
				coreptLib.dspMsg("Select Data Supplier.");
				goItem("tld_std_airports.data_supplier");
				throw new FormTriggerFailureException(event);
			}

			goBlock("compareActCust", "");
			clearBlock("compareActCust", "noCommit");

			tldArptCur = "SELECT tld_apt.airport_ident tld_arpt, tld_apt.customer_ident cust_arpt"
					+ "         FROM pl_tld_airport tld_apt, " + "         			pl_std_airport std_apt"
					+ "        WHERE tld_apt.data_supplier     = 	std_apt.data_supplier"
					+ "          AND tld_apt.processing_cycle  = 	std_apt.processing_cycle"
					+ "          AND tld_apt.airport_ident 		= 	std_apt.airport_ident"
					+ "          AND tld_apt.processing_cycle 	= 	?"
					+ "          AND tld_apt.data_supplier 		= 	?"
					+ "          AND tld_apt.airport_ident 	LIKE 	'' || NVL(?,'%') || ''"
					+ "     GROUP BY tld_apt.airport_ident, tld_apt.customer_ident" + "     ORDER BY 1, 2";
			records = app.executeQuery(tldArptCur, tldStdAirports.getProcessingCycle(),
					tldStdAirports.getDataSupplier(), tldStdAirports.getAirportIdent());

			if (records == null || records.isEmpty()) {
				tldStdAirports.setTotalCustomers(0);
				coreptLib.dspMsg("None of the Tailored Airport(s) is/are Associated with \nCustomer Ident(s).");
				goBlock("tldStdAirports", "");
				goItem("tld_std_airports.processing_cycle");
				OracleHelpers.ResponseMapper(this, resDto);
				throw new FormTriggerFailureException();
			} else {
				for (Record record : records) {

					CompareActCust airportsFindCust = new CompareActCust();
					airportsFindCust.setAirportIdent(record.getString());
					airportsFindCust.setCustIdent(record.getString());
					compareActCust.add(airportsFindCust);
					recCount++;
				}
				compareActArpt.getData().clear();
				tldStdAirports.setTotalCustomers(recCount);
			}
			firstRecord("");

			for (int i = 0; i < event.size(); i++) {
				Event eve = event.get(i);
				if (eve.getName().equals("setAlertProperty")) {
					Event moveto = event.remove(i);
					event.add(event.size(), moveto);
					i--;

				}
				if (eve.getName().equals("showAlert")) {
					Event moveto = event.remove(i);
					event.add(moveto);
					break;
				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStdAirportsFindCustWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStdAirportsFindCustWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> tldStdAirportsClearWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStdAirportsClearWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goBlock("compareActArpt", "");
			clearBlock("compareActArpt", "noCommit");

			goBlock("compareActCust", "");
			clearBlock("compareActCust", "noCommit");

			goBlock("tldStdAirports", "");
			clearBlock("tldStdAirports", "noCommit");

			compareActArpt.getData().clear();
			compareActCust.getData().clear();

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStdAirportsClearWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStdAirportsClearWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> stdRecordCountBlkProcessingCycleWhenListChanged(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRecordCountBlkProcessingCycleWhenListChanged Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String lsCycle = stdRecordCountBlk.getProcessingCycle();
			String lsPrevCycle = "";

			// TODO ls_prev_cycle := util2.get_previous_cycle(ls_cycle)
			lsPrevCycle = toString(app.executeFunction(Integer.class, "CPT", "get_previous_cycle", "util2",
					OracleTypes.INTEGER, new ProcedureInParameter("p_cycle", lsCycle, OracleTypes.INTEGER)));

			if (lsPrevCycle == null) {
				lsCycle = "";
				lsPrevCycle = "";
			}

			setItemProperty("compare_record_count_blk.count1", "prompt_text", lsPrevCycle + " Count");
			setItemProperty("compare_record_count_blk.count2", "prompt_text", lsCycle + " Count");
			goBlock("compareRecordCountBlk", "");
			// TODO CLEAR_BLOCK(NO_COMMIT);
			clearBlock("compareRecordCountBlk", "noCommit");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRecordCountBlkProcessingCycleWhenListChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRecordCountBlkProcessingCycleWhenListChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> stdRecordCountBlkPopulateCountWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRecordCountBlkPopulateCountWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();

		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			compareRecordCountBlk.getData().clear();
			String recCnt = """
					select rc1.table_name,rc1.data_supplier,rc1.new_cycle as CYCLE1,rc1.records_changed+rc1.records_added+rc1.records_unchanged as TOTAL_REC1,
					        rc2.new_cycle as CYCLE2,rc2.records_changed+rc2.records_added+rc2.records_unchanged as TOTAL_REC2,
					        rc1.records_changed,rc1.records_deleted,rc1.records_added,rc1.records_unchanged
					        from cptm_record_count rc1, cptm_record_count rc2
					        where rc1.new_cycle = ?
					        and rc2.new_cycle = ?
					        and rc1.data_supplier = ?
					        and rc1.data_supplier = rc2.data_supplier
					        and rc1.table_name = rc2.table_name
					        and rc1.table_name like '%STD%'
					        and rc2.table_name like '%STD%'
					        order by rc1.table_name
					""";
			Object lsCurrCycle = null;
			Object lsPrevCycle = null;
			Integer cnt = 0;

			goBlock("compareRecordCountBlk", "");

			clearBlock("compareRecordCountBlk", "");
			if (Objects.equals(stdRecordCountBlk.getProcessingCycle(), null)
					|| Objects.equals(stdRecordCountBlk.getProcessingCycle(), "")) {
				coreptLib.dspMsg("Select Processing Cycle.");
				goItem("std_record_count_blk.processing_cycle");
				throw new FormTriggerFailureException(event);

			}

			if (Objects.equals(stdRecordCountBlk.getDataSupplier(), null)
					|| Objects.equals(stdRecordCountBlk.getDataSupplier(), "")) {
				coreptLib.dspMsg("Select Data Supplier.");
				goItem("std_record_count_blk.data_supplier");
				throw new FormTriggerFailureException(event);
			}

			lsCurrCycle = stdRecordCountBlk.getProcessingCycle();
			lsPrevCycle = app.executeFunction(Integer.class, "CPT", "get_previous_cycle", "util2", OracleTypes.INTEGER,
					new ProcedureInParameter("p_cycle", lsCurrCycle, OracleTypes.INTEGER));
			List<Record> records = app.executeQuery(recCnt, stdRecordCountBlk.getProcessingCycle(), lsPrevCycle,
					stdRecordCountBlk.getDataSupplier());
			for (Record record : records) {
				CompareRecordCountBlk compareRecordCountBlks = new CompareRecordCountBlk();
				compareRecordCountBlks.setDataType(record.getString());
				compareRecordCountBlks.setSupplier(record.getString());
				Integer cycle1 = record.getInt();
				Integer totalRec1 = record.getInt();
				Integer cycle2 = record.getInt();
				Integer totalRec2 = record.getInt();
				Integer recordsChanged = record.getInt();
				Integer recordsDeleted = record.getInt();
				Integer recordsAdded = record.getInt();
				Integer recordsUnchanged = record.getInt();
				compareRecordCountBlks.setCount1(totalRec2);
				compareRecordCountBlks.setCount2(totalRec1);
				compareRecordCountBlks.setCycle1(cycle1);
				compareRecordCountBlks.setCycle2(cycle2);
				compareRecordCountBlks.setAdded(recordsAdded);
				compareRecordCountBlks.setDeleted(recordsDeleted);
				compareRecordCountBlks.setChanged(recordsChanged);
				compareRecordCountBlks.setUnchanged(recordsUnchanged);
				compareRecordCountBlk.add(compareRecordCountBlks);
				cnt = cnt + 1;
				nextRecord("compareRecordCountBlk");
			}

			stdRecordCountBlk.setPopulateCount(toString(cnt));

			firstRecord("compareRecordCountBlk");
			if (Objects.equals(cnt, 0)) {
				compareRecordCountBlk.getData().clear();
				coreptLib.dspMsg("No Standard Data exists for Selected Cycle");
				throw new FormTriggerFailureException(event);
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRecordCountBlkPopulateCountWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRecordCountBlkPopulateCountWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> stdRecordCountBlkClearWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRecordCountBlkClearWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapper(reqDto, this);
			compareRecordCountBlk.getData().clear();

			goBlock("compareActArpt", "");

			clearBlock("compareActArpt", "");
			goBlock("compareActCust", "");

			clearBlock("compareActCust", "");
			goBlock("tldStdAirports", "");

			clearBlock("tldStdAirports", "");
			goBlock("stdRecordCountBlk", "");

			clearBlock("stdRecordCountBlk", "");
			goBlock("compareRecordCountBlk", "");

			clearBlock("compareRecordCountBlk", "");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRecordCountBlkClearWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRecordCountBlkClearWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> stdRecordCountBlkExportWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRecordCountBlkExportWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO export_form_csv.main("COMPARE_RECORD_COUNT_BLK");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRecordCountBlkExportWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRecordCountBlkExportWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilDummyWhenButtonPressed(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilDummyWhenButtonPressed Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilClientinfoFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilFileFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilHostFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilSessionFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilFiletransferFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilOleFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilCApiFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> webutilWebutilBrowserFunctionsWhenCustomItemEvent(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<CompareActivityTriggerResponseDto>> toolsExportDestination(
			CompareActivityTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<CompareActivityTriggerResponseDto> responseObj = new BaseResponse<>();
		CompareActivityTriggerResponseDto resDto = new CompareActivityTriggerResponseDto();
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
			BlockDetail mstBlockData = null;
			// String Builders
			StringBuilder reportfile = new StringBuilder();
			List<Record> recs = null;
			// Master Block
			if (OracleHelpers.isNullorEmpty(selectOptions.getFileName())) {
				PropertyHelpers.setAlertProperty(event, "dsp_msg", "stop", "Forms",
						"WUT-130: Client file name cannot be null", "ALERT_MESSAGE_TEXT", "OK", null, null);
				PropertyHelpers.setShowAlert(event, "dsp_msg", false);
				throw new FormTriggerFailureException();
			}
			if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("compareRecordCountBlk")) {
				mstBlockData = reqDto.getExportDataBlocks().get("compareRecordCountBlk");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = """
						select rc1.table_name,rc1.data_supplier,rc1.new_cycle as CYCLE1,rc1.records_changed+rc1.records_added+rc1.records_unchanged as TOTAL_REC1,
						       rc2.new_cycle as CYCLE2,rc2.records_changed+rc2.records_added+rc2.records_unchanged as TOTAL_REC2,
						       rc1.records_changed,rc1.records_deleted,rc1.records_added,rc1.records_unchanged
						       from cptm_record_count rc1, cptm_record_count rc2
						       where rc1.new_cycle = ?
						       and rc2.new_cycle = ?
						       and rc1.data_supplier = ?
						       and rc1.data_supplier = rc2.data_supplier
						       and rc1.table_name = rc2.table_name
						       and rc1.table_name like '%STD%'
						       and rc2.table_name like '%STD%'
						       order by rc1.table_name
						""";
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				Integer lsCurrCycle = toInteger(mstBlockData.getProcessingCycle());
				Integer lsPrevCycle = app.executeFunction(Integer.class, "CPT", "get_previous_cycle", "util2",
						OracleTypes.INTEGER, new ProcedureInParameter("p_cycle", lsCurrCycle, OracleTypes.INTEGER));

				recs = app.executeQuery(query, mstBlockData.getProcessingCycle(), lsPrevCycle,
						mstBlockData.getDataSupplier());
				CompareRecordCountBlk compareRecordCountBlk = new CompareRecordCountBlk();
				for (Record mstRec : recs) {
					compareRecordCountBlk.setDataType(toString(mstRec.getObject("TABLE_NAME")));
					compareRecordCountBlk.setSupplier(toString(mstRec.getObject("DATA_SUPPLIER")));
					compareRecordCountBlk.setCycle1(toInteger(mstRec.getObject("CYCLE1")));
					compareRecordCountBlk.setCount1(toInteger(mstRec.getObject("TOTAL_REC2")));
					compareRecordCountBlk.setCycle2(toInteger(mstRec.getObject("CYCLE2")));
					compareRecordCountBlk.setCount2(toInteger(mstRec.getObject("TOTAL_REC1")));
					compareRecordCountBlk.setChanged(toInteger(mstRec.getObject("RECORDS_CHANGED")));
					compareRecordCountBlk.setDeleted(toInteger(mstRec.getObject("RECORDS_DELETED")));
					compareRecordCountBlk.setAdded(toInteger(mstRec.getObject("RECORDS_ADDED")));
					compareRecordCountBlk.setUnchanged(toInteger(mstRec.getObject("RECORDS_UNCHANGED")));
					reportfile.append(getExportData(compareRecordCountBlk, mstDatabseColumns, 0,
							selectOptions.getDelimiter(), selectOptions.getGetTextFile()));
				}

			}
			OracleHelpers.ResponseMapper(this, resDto);
			String base64 = Base64.getEncoder().encodeToString(reportfile.toString().getBytes(StandardCharsets.UTF_8));
			ReportDetail reportDetail = new ReportDetail();
			reportDetail.setData(base64);
			resDto.setReport(reportDetail);
			goBlock(system.getCursorBlock(), "firstItem");
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

	public void updateAppInstance() throws SQLException {
		// app.getConnection();

		OracleHelpers.bulkClassMapper(this, coreptLib);
		coreptLib.initialization(this);
		OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		coreptMenuMmbServiceImpl.initialization(this);
		super.app = this.app;
		super.groups = this.groups;
		super.genericNativeQueryHelper = this.genericNativeQueryHelper;
		super.event = this.event;
		super.system = this.system;
		super.global = this.global;
		super.baseInstance = this;
		super.displayAlert = this.displayAlert;

	}
}
