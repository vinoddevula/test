package com.honeywell.coreptdu.datatypes.runway.serviceimpl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.NonUniqueObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.coreptmenummb.serviceimpl.CoreptMenuMmbServiceImpl;
import com.honeywell.coreptdu.datatypes.corepttemplate.block.DisplayItemBlock;
import com.honeywell.coreptdu.datatypes.corepttemplate.serviceimpl.CoreptTemplateTriggerServiceImpl;
import com.honeywell.coreptdu.datatypes.exportdestination.block.SelectOptions;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.request.ExportDestinationTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.response.ExportDestinationTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.runway.block.ControlBlock;
import com.honeywell.coreptdu.datatypes.runway.block.Webutil;
import com.honeywell.coreptdu.datatypes.runway.dto.request.RunwayTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.runway.dto.response.RunwayTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.runway.entity.PlStdRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.PlTldRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.StdRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.TldRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.service.IRunwayTriggerService;
import com.honeywell.coreptdu.exception.AlertException;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.exception.FormTriggerFailureException;
import com.honeywell.coreptdu.global.dbtype.CrRunway;
import com.honeywell.coreptdu.global.dbtype.PlStdRunway;
import com.honeywell.coreptdu.global.dbtype.PlTldRunway;
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
import com.honeywell.coreptdu.pkg.body.DisplayAlert;
import com.honeywell.coreptdu.pkg.body.RefreshMasterLibrary;
import com.honeywell.coreptdu.pll.CoreptLib;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.GenericNativeQueryHelper;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInOutParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureOutParameter;
import com.honeywell.coreptdu.utils.oracleutils.Block;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.HoneyWellUtils;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.PropertyHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroup;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;

@Slf4j
@Service
@RequestScope
public class RunwayTriggerServiceImpl extends GenericTemplateForm<RunwayTriggerServiceImpl>
		implements IRunwayTriggerService {

	@Getter
	@Setter
	private Webutil webutil = new Webutil();
	@Getter
	@Setter
	private Block<PlTldRunwayMr> plTldRunwayMr = new Block<>();
	@Getter
	@Setter
	private ControlBlock controlBlock = new ControlBlock();
	@Getter
	@Setter
	private Block<PlStdRunwayMr> plStdRunwayMr = new Block<>();
	@Getter
	@Setter
	private Block<TldRunwayMr> tldRunwayMr = new Block<>();
	@Getter
	@Setter
	private Block<StdRunwayMr> stdRunwayMr = new Block<>();
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
	private List<Event> event = new ArrayList<>();
	@Getter
	@Setter
	private HashMap<String, RecordGroup> groups = new HashMap<>();
	@Getter
	@Setter
	private List<String> blocksOrder = new ArrayList<>();
	@Getter
	@Setter
	private Map<String, WindowDetail> windows = new HashMap<>();

	@Autowired
	@Getter
	@Setter
	private IApplication app;

	@Autowired
	private CoreptLib coreptLib;

	@Autowired
	private CoreptTemplateTriggerServiceImpl coreptTemplate;

	@Autowired
	private RefreshMasterLibrary refreshMasterLibrary;

	@Autowired
	private HashUtils hashUtils;

	@Getter
	@Setter
	private SelectOptions selectOptions = new SelectOptions();

	@Autowired
	private CoreptMenuMmbServiceImpl coreptMenuMmbServiceImpl;

	@Getter
	@Setter
	@Autowired
	private DisplayAlert displayAlert = new DisplayAlert();
	@Getter
	@Setter
	private AlertDetail alertDetails = new AlertDetail();

	@Autowired
	private GenericNativeQueryHelper genericNativeQueryHelper;

	public void updateAppInstance() {

		super.app = this.app;
		super.baseInstance = this;
		super.groups = this.groups;
		super.genericNativeQueryHelper = this.genericNativeQueryHelper;
		super.event = this.event;
		super.parameter = this.parameter;
		super.displayAlert = this.displayAlert;
		this.system.setCursorBlock(toSnakeCase(system.getCursorBlock()));
		super.system = this.system;
		super.global = this.global;
		super.blocksOrder = this.blocksOrder;
		super.windows = this.windows;
		// super.displayItemBlock = this.displayItemBlock;
		OracleHelpers.bulkClassMapper(this, displayAlert);
		OracleHelpers.bulkClassMapper(this, coreptLib);
		coreptLib.initialization(this);
		OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		coreptMenuMmbServiceImpl.initialization(this);
		OracleHelpers.bulkClassMapper(this, coreptTemplate);
		coreptTemplate.initialization(this);
		OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		refreshMasterLibrary.initialization(this);

	}

	// TODO PUnits Manual configuration
	// ParentName ---> CHECK_TO_COMMIT
	// File Name ---> corept_template.fmb
	// TODO PUnits Manual configuration
	// ParentName ---> DSP_ERROR
	// File Name ---> corept_template.fmb

	@Override
	public void doValidate(String pBlock, String pIgnoreRef) throws Exception {
		log.info("doValidate Executing");
		// String query = "";
		// Record rec = null;
		try {
			// Object vRecord = null;
			Integer vErrInd = 0;
			List<Integer> vErrList = new ArrayList<>();
			String vAllErr = null;
			PlStdRunway vSrec = new PlStdRunway();
			PlTldRunway vTrec = new PlTldRunway();
			String vNerr = "YYYYY";
			String vValid = null;
			Integer vCycle = null;
			String vInd = "Y";
			CrRunway vRecord = new CrRunway();

			if (!Objects.equals(toString(
					nameIn(this, pBlock + ".RUNWAY_IDENT")),
					null)) {

				// TODO Set_Application_Property(cursor_style,"BUSY");
				if (Objects.equals(parameter.getRecordType(), "S")) {
					controlBlock.setStdOverrideErrors(null);

				}

				else {
					controlBlock.setTldOverrideErrors(null);

				}

				// TODO populate_record(p_block,v_record,v_cycle) --- Program Unit Calling
				Map<String, Object> res = populateRecord(pBlock);
				vRecord = (CrRunway) res.get("pRecord");
				vCycle = (Integer) res.get("pCycle");

				// TODO
				// RECSV1.VRUNWAY(global.getDataSupplier(),v_cycle,v_record,v_err_list,v_err_ind);
				String vcyc = toString(vCycle);
				Map<String, Object> dbCall = app.executeProcedure("CPT", "VRUNWAY", "RECSV1",
						new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_proc_cycle", vcyc, OracleTypes.VARCHAR),
						new ProcedureInOutParameter("p_crecord", vRecord, OracleTypes.STRUCT, "SDV_RECORDS.CR_RUNWAY"),
						new ProcedureOutParameter("p_errlist", OracleTypes.ARRAY, "CPT_TYPES.ERRLIST_TYPE"),
						new ProcedureOutParameter("v_err_ind", OracleTypes.NUMBER));

				Struct outStruct = (Struct) dbCall.get("p_crecord");
				// Object[] updatedRecord = outStruct.getAttributes();
				outStruct.getAttributes();
				Array errList = (Array) dbCall.get("p_errlist");
				BigDecimal[] bg = (BigDecimal[]) errList.getArray();
				vErrInd = toInteger(dbCall.get("v_err_ind"));

				for (BigDecimal val : bg) {
					vErrList.add(val.intValue());
				}

				if (!Objects.equals(vErrInd, 0)) {
					for (int i = 0; i <= vErrList.size() - 1; i++) {
						if (!coreptLib.isOverride(global.getDataSupplier(), vCycle, "RUNWAY",
								vErrList.get(i))) {
							vAllErr = getNullClean(vAllErr) + " * " + toChar(vErrList.get(i)) + " - "
									+ coreptLib.getErrText(vErrList.get(i));
							vInd = "I";
						}

						else {
							if (!Objects.equals(vInd, "I")) {
								vInd = "O";

							}

						}

					}

				}

				// TODO populate_rel_record(p_block,v_srec,v_trec) --- Program Unit Calling
				Map<String, Object> res1 = populateRelRecord(pBlock);
				// PlTldRunway
				vSrec = Optional.ofNullable((PlStdRunway) res1.get("pSrec")).orElseGet(PlStdRunway::new);

				//vSrec = (PlStdRunway) res1.get("pSrec");
				vTrec = (PlTldRunway) res1.get("pTrec");

				// TODO
				// recrv1.vrunway(parameter.getRecordType(),v_srec,v_trec,v_nerr,v_valid,p_ignore_ref,"DU");

				Map<String, Object> dbCall1 = app.executeProcedure("CPTS", "vrunway_wrapper", "RECRV1_WRAPPER",
						new ProcedureInParameter("p_record_type", parameter.getRecordType(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_srec", vSrec, OracleTypes.STRUCT,
								"RECRV1_WRAPPER.PL_STD_RUNWAY_TYPE"),
						new ProcedureInParameter("p_trec", vTrec, OracleTypes.STRUCT,
								"RECRV1_WRAPPER.PL_TLD_RUNWAY_TYPE"),
						new ProcedureOutParameter("p_err", OracleTypes.VARCHAR),
						new ProcedureOutParameter("p_valind", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_view_only", pIgnoreRef, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_run_loc", "DU", OracleTypes.VARCHAR));

				vNerr = (String) dbCall1.get("p_err");
				vValid = (String) dbCall1.get("p_valind");

				// coverity-fixes
				log.info(vValid);
				// dbCall1.get("p_valind");

				if (!Objects.equals(vNerr, "YYYYY")) {
					if (Objects.equals(substr(vNerr, 1, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 216 - " + coreptLib.getErrText(216);
					}

					if (Objects.equals(substr(vNerr, 2, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 266 - " + coreptLib.getErrText(266);

					}

					if (Objects.equals(substr(vNerr, 3, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 648 - " + coreptLib.getErrText(648);

					}

					if (Objects.equals(substr(vNerr, 4, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 655 - " + coreptLib.getErrText(655);

					}

					if (Objects.equals(substr(vNerr, 5, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 1167 - " + coreptLib.getErrText(1167);

					}

				}

				// TODO
				// Set_Ind_and_Message(p_block,v_all_err,parameter.getWorkType(),p_ignore_ref,v_ind);
				coreptLib.setindandmessage(pBlock, vAllErr, parameter.getWorkType(), pIgnoreRef, vInd);

				if (Objects.equals(pIgnoreRef, "N")) {

					// TODO SET_UPDATE_DCR(upper(p_block),v_record) --- Program Unit Calling
					setUpdateDcr(upper(pBlock), vRecord);

				}

				// TODO Set_Override_Button(p_block);
				coreptLib.setoverridebutton(pBlock);
				// TODO Set_Application_Property(cursor_style,"DEFAULT");

			}

			else {

				// TODO set_initial_error_display(P_BLOCK);
				coreptLib.setinitialerrordisplay(pBlock);
			}

			log.info("doValidate Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing doValidate" + e.getMessage());
			throw e;

		}
	}

	@Override
	public Map<String, Object> populateRecord(String pBlock) throws Exception {
		// public void populateRecord(String pBlock,Integer pCycle,Object pRecord)
		// throws Exception{
		log.info("populateRecord Executing");
		Map<String, Object> res = new HashMap<>();
		// String query = "";
		// Record rec = null;
		Integer pCycle = null;
		CrRunway pRecord = new CrRunway();
		try {
			// TODO Configure the Out Params --> p_record
			// TODO Configure the Out Params --> p_cycle

//			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_SR")) {
//				pRecord.setRecordType(parameter.getRecordType());
//				pRecord.setAirportIcao(PlStdRunwaySr.getAirportIcao());
//				pRecord.setAirportIdent(PlStdRunwaySr.getAirportIdent());
//				pRecord.setCustAreaCode(PlStdRunwaySr.getAreaCode());
//				pRecord.setCycleData(PlStdRunwaySr.getCycleData());
//				pRecord.setDisplacedThresholdDistance(
//						lpad(toChar(PlStdRunwaySr.getDisplacedThresholdDistance()), 4, '0'));
//				pRecord.setFileRecno(lpad(toChar(PlStdRunwaySr.getFileRecno()), 5, '0'));
//				pRecord.setLandingThresholdElevation(toChar(PlStdRunwaySr.getLandingThresholdElevation()));
//				pRecord.setLocalizerMlsClass(PlStdRunwaySr.getLocalizerMlsClass());
//				pRecord.setLocalizerMlsClass(PlStdRunwaySr.getLocalizerMlsGlsIdent());
//				pRecord.setRunwayDescription(PlStdRunwaySr.getRunwayDescription());
//				pRecord.setRunwayGradient(rpad(PlStdRunwaySr.getRunwayGradient(), 5, " "));
//				pRecord.setRunwayIdent(rpad(PlStdRunwaySr.getRunwayIdent(), 5, " "));
//				pRecord.setRunwayLatitude(PlStdRunwaySr.getRunwayLatitude());
//				pRecord.setRunwayLength(lpad(toChar(PlStdRunwaySr.getRunwayLength()), 5, '0'));
//				pRecord.setRunwayLongitude(PlStdRunwaySr.getRunwayLongitude());
//				pRecord.setRunwayMagneticBearing(PlStdRunwaySr.getRunwayMagneticBearing());
//				pRecord.setRunwayWidth(lpad(toChar(PlStdRunwaySr.getRunwayWidth()), 3, '0'));
//				pRecord.setSecondLocalizerClass(PlStdRunwaySr.getSecondLocalizerClass());
//				pRecord.setLocalizerMlsGlsIdent(PlStdRunwaySr.getSecondLocalizerMlsGlsIdent());
//				pRecord.setStopway(lpad(toString(PlStdRunwaySr.getStopway()), 4, '0'));
//				pRecord.setThresholdCrossingHeight(lpad(toChar(PlStdRunwaySr.getThresholdCrossingHeight()), 2, '0'));
//				pCycle = PlStdRunwaySr.getProcessingCycle();
//				pRecord.setRunwayAccuracyCompInd(PlStdRunwaySr.getRunwayAccuracyCompInd());
//				pRecord.setLandThreselevAccrCompInd(PlStdRunwaySr.getLandThreselevAccrCompInd());
//
//			}

			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_MR")) {
				pRecord.setRecordType(parameter.getRecordType());
				pRecord.setAirportIcao(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pRecord.setAirportIdent(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pRecord.setCustAreaCode(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAreaCode());
				pRecord.setCycleData(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getCycleData());
				pRecord.setDisplacedThresholdDistance(lpad(
						toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getDisplacedThresholdDistance()), 4,
						'0'));
				pRecord.setFileRecno(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getFileRecno()), 5, '0'));
				pRecord.setLandingThresholdElevation(
						toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLandingThresholdElevation()));
				pRecord.setLocalizerMlsClass(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pRecord.setLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pRecord.setRunwayDescription(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayDescription());
				pRecord.setRunwayGradient(
						rpad(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayGradient(), 5, " "));
				pRecord.setRunwayIdent(
						rpad(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent(), 5, " "));
				pRecord.setRunwayLatitude(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLatitude());
				pRecord.setRunwayLength(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLength()), 5, '0'));
				pRecord.setRunwayLongitude(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLongitude());
				pRecord.setRunwayMagneticBearing(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());
				pRecord.setRunwayWidth(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayWidth()), 3, '0'));
				pRecord.setSecondLocalizerClass(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pRecord.setSecondLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				pRecord.setStopway(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getStopway()), 4, '0'));
				pRecord.setThresholdCrossingHeight(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getThresholdCrossingHeight()),
								2, '0'));
				pCycle = toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle());
				pRecord.setRunwayAccuracyCompInd(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayAccuracyCompInd());
				pRecord.setLandThreselevAccrCompInd(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLandThreselevAccrCompInd());

			}

//			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_SR")) {
//				pRecord.setRecordType(parameter.getRecordType());
//				pRecord.setAirportIcao(PlTldRunwaySr.getAirportIcao());
//				pRecord.setAirportIdent(PlTldRunwaySr.getAirportIdent());
//				pRecord.setCustAreaCode(PlTldRunwaySr.getCustomerIdent());
//				pRecord.setCycleData(PlTldRunwaySr.getCycleData());
//				pRecord.setDisplacedThresholdDistance(
//						lpad(toChar(PlTldRunwaySr.getDisplacedThresholdDistance()), 4, '0'));
//				pRecord.setFileRecno(lpad(toChar(PlTldRunwaySr.getFileRecno()), 5, '0'));
//				pRecord.setLandingThresholdElevation(toChar(PlTldRunwaySr.getLandingThresholdElevation()));
//				pRecord.setLocalizerMlsClass(PlTldRunwaySr.getLocalizerMlsClass());
//				pRecord.setLocalizerMlsGlsIdent(PlTldRunwaySr.getLocalizerMlsGlsIdent());
//				pRecord.setRunwayDescription(PlTldRunwaySr.getRunwayDescription());
//				pRecord.setRunwayGradient(rpad(PlTldRunwaySr.getRunwayGradient(), 5, " "));
//				pRecord.setRunwayIdent(rpad(PlTldRunwaySr.getRunwayIdent(), 5, " "));
//				pRecord.setRunwayLatitude(PlTldRunwaySr.getRunwayLatitude());
//				pRecord.setRunwayLength(lpad(toChar(PlTldRunwaySr.getRunwayLength()), 5, '0'));
//				pRecord.setRunwayLongitude(PlTldRunwaySr.getRunwayLongitude());
//				pRecord.setRunwayMagneticBearing(PlTldRunwaySr.getRunwayMagneticBearing());
//				pRecord.setRunwayWidth(lpad(toChar(PlTldRunwaySr.getRunwayWidth()), 3, '0'));
//				pRecord.setSecondLocalizerClass(PlTldRunwaySr.getSecondLocalizerClass());
//				pRecord.setSecondLocalizerMlsGlsIdent(PlTldRunwaySr.getSecondLocalizerMlsGlsIdent());
//				pRecord.setStopway(lpad(toChar(PlTldRunwaySr.getStopway()), 4, '0'));
//				pRecord.setThresholdCrossingHeight(lpad(toChar(PlTldRunwaySr.getThresholdCrossingHeight()), 2, '0'));
//				pCycle = PlTldRunwaySr.getProcessingCycle();
//				pRecord.setRunwayAccuracyCompInd(PlTldRunwaySr.getRunwayAccuracyCompInd());
//				pRecord.setLandThreselevAccrCompInd(PlTldRunwaySr.getLandThreselevAccrCompInd());
//
//			}

			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_MR")) {
				pRecord.setRecordType(parameter.getRecordType());
				pRecord.setAirportIcao(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pRecord.setAirportIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pRecord.setCustAreaCode(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCustomerIdent());
				pRecord.setCycleData(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCycleData());
				pRecord.setDisplacedThresholdDistance(lpad(
						toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getDisplacedThresholdDistance()), 4,
						'0'));
				pRecord.setFileRecno(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getFileRecno()), 5, '0'));
				pRecord.setLandingThresholdElevation(
						toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLandingThresholdElevation()));
				pRecord.setLocalizerMlsClass(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pRecord.setLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pRecord.setRunwayDescription(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayDescription());
				pRecord.setRunwayGradient(
						rpad(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayGradient(), 5, " "));
				pRecord.setRunwayIdent(
						rpad(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent(), 5, " "));
				pRecord.setRunwayLatitude(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLatitude());
				pRecord.setRunwayLength(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLength()), 5, '0'));
				pRecord.setRunwayLongitude(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLongitude());
				pRecord.setRunwayMagneticBearing(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());
				pRecord.setRunwayWidth(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayWidth()), 3, '0'));
				pRecord.setSecondLocalizerClass(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pRecord.setSecondLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				pRecord.setStopway(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getStopway()), 4, '0'));
				pRecord.setThresholdCrossingHeight(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getThresholdCrossingHeight()),
								2, '0'));
				pCycle = toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle());
				pRecord.setRunwayAccuracyCompInd(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayAccuracyCompInd());
				pRecord.setLandThreselevAccrCompInd(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLandThreselevAccrCompInd());

			}
			res.put("pRecord", pRecord);
			res.put("pCycle", pCycle);

			log.info("populateRecord Executed Successfully");
			return res;
		} catch (Exception e) {
			log.error("Error while executing populateRecord" + e.getMessage());
			throw e;

		}
	}

	// TODO PUnits Manual configuration
	// ParentName ---> INITIALIZE_FORM
	// File Name ---> corept_template.fmb
	// TODO PUnits Manual configuration
	// ParentName ---> POPULATE_ITEMS
	// File Name ---> airport.fmb

	@Override
	public Map<String, Object> populateRelRecord(String pBlock) throws Exception {
		log.info("populateRelRecord Executing");
		Map<String, Object> res = new HashMap<>();
		PlStdRunway pSrec = new PlStdRunway();
		PlTldRunway pTrec = new PlTldRunway();
		try {
			// TODO Configure the Out Params --> p_srec
			// TODO Configure the Out Params --> p_trec

//			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_SR")) {
//				pSrec.setAirportIcao(PlStdRunwaySr.getAirportIcao());
//				pSrec.setAirportIdent(PlStdRunwaySr.getAirportIdent());
//				pSrec.setRunwayIdent(PlStdRunwaySr.getRunwayIdent());
//				pSrec.setValidateInd(PlStdRunwaySr.getValidateInd());
//				pSrec.setLocalizerMlsGlsIdent(PlStdRunwaySr.getLocalizerMlsGlsIdent());
//				pSrec.setSecondLocalizerMlsGlsIdent(PlStdRunwaySr.getSecondLocalizerMlsGlsIdent());
//				if (toInteger(global.getRecentCycle()) >= PlStdRunwaySr.getProcessingCycle()) {
//					pSrec.setProcessingCycle(PlStdRunwaySr.getProcessingCycle());
//
//				}
//
//				else {
//					pSrec.setProcessingCycle(toInteger(global.getRecentCycle()));
//
//				}
//				pSrec.setAreaCode(PlStdRunwaySr.getAreaCode());
//				pSrec.setDataSupplier(PlStdRunwaySr.getDataSupplier());
//				pSrec.setCreateDcrNumber(PlStdRunwaySr.getCreateDcrNumber());
//				pSrec.setLocalizerMlsClass(PlStdRunwaySr.getLocalizerMlsClass());
//				pSrec.setSecondLocalizerClass(PlStdRunwaySr.getSecondLocalizerClass());
//				pSrec.setRunwayMagneticBearing(PlStdRunwaySr.getRunwayMagneticBearing());
//
//			}

			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_MR")) {
				pSrec.setAirportIcao(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pSrec.setAirportIdent(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pSrec.setRunwayIdent(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent());
				pSrec.setValidateInd(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getValidateInd());
				pSrec.setLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pSrec.setSecondLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				if (toInteger(global.getRecentCycle()) >= toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex())
						.getProcessingCycle())) {
					pSrec.setProcessingCycle(toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()));

				}

				else {
					pSrec.setProcessingCycle(toInteger(global.getRecentCycle()));

				}
				pSrec.setAreaCode(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAreaCode());
				pSrec.setDataSupplier(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getDataSupplier());
				pSrec.setCreateDcrNumber(toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				pSrec.setLocalizerMlsClass(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pSrec.setSecondLocalizerClass(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pSrec.setRunwayMagneticBearing(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());

			}

//			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_SR")) {
//				pTrec.setAirportIcao(PlTldRunwaySr.getAirportIcao());
//				pTrec.setAirportIdent(PlTldRunwaySr.getAirportIdent());
//				pTrec.setRunwayIdent(PlTldRunwaySr.getRunwayIdent());
//				pTrec.setValidateInd(PlTldRunwaySr.getValidateInd());
//				pTrec.setLocalizerMlsGlsIdent(PlTldRunwaySr.getLocalizerMlsGlsIdent());
//				pTrec.setSecondLocalizerMlsGlsIdent(PlTldRunwaySr.getSecondLocalizerMlsGlsIdent());
//				pTrec.setCustomerIdent(PlTldRunwaySr.getCustomerIdent());
//				pTrec.setDataSupplier(PlTldRunwaySr.getDataSupplier());
//				pTrec.setGeneratedInHouseFlag(PlTldRunwaySr.getGeneratedInHouseFlag());
//				if (toInteger(global.getRecentCycle()) >= PlTldRunwaySr.getProcessingCycle()) {
//
//					pTrec.setProcessingCycle(PlTldRunwaySr.getProcessingCycle());
//
//				}
//
//				else {
//					pTrec.setProcessingCycle(toInteger(global.getRecentCycle()));
//
//				}
//				pTrec.setCreateDcrNumber(PlTldRunwaySr.getCreateDcrNumber());
//				pTrec.setLocalizerMlsClass(PlTldRunwaySr.getLocalizerMlsClass());
//				pTrec.setSecondLocalizerClass(PlTldRunwaySr.getSecondLocalizerClass());
//				pTrec.setRunwayMagneticBearing(PlTldRunwaySr.getRunwayMagneticBearing());
//
//			}

			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_MR")) {
				pTrec.setAirportIcao(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pTrec.setAirportIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pTrec.setRunwayIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent());
				pTrec.setValidateInd(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getValidateInd());
				pTrec.setLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pTrec.setSecondLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				pTrec.setCustomerIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCustomerIdent());
				pTrec.setDataSupplier(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getDataSupplier());
				pTrec.setGeneratedInHouseFlag(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getGeneratedInHouseFlag());
				pTrec.setCreateDcrNumber(toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				pTrec.setProcessingCycle(toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()));
				pTrec.setLocalizerMlsClass(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pTrec.setSecondLocalizerClass(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pTrec.setRunwayMagneticBearing(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());

			}

			res.put("pSrec", pSrec);
			res.put("pTrec", pTrec);

			log.info("populateRelRecord Executed Successfully");
			return res;
		} catch (Exception e) {
			log.error("Error while executing populateRelRecord" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String checkRunwayRef(String pTable, String pRunwayIdent, Integer pCycle, String pCust, String pDcr)
			throws Exception {
		log.info("checkRunwayRef Executing");
		String query = "";
		Record rec = null;
		try {
			Integer vCount = null;
			String vStmt = null;
			// Integer vDcrNumber = 0;
			String vReferencingTable = null;
			String vBRunway = "N";
			String vType = null;
			String vBlock = system.getCursorBlock();
			String vAirportIdent = toChar(nameIn(this, vBlock + ".AIRPORT_IDENT"));
			String vAirportIcao = toChar(nameIn(this, vBlock + ".AIRPORT_ICAO"));
			String getRefInfo = """
					select unique lower(REFERENCING_TABLE_NAME)
					  	from ref_table
					   	where data_supplier = ?
					   	and processing_cycle = ?
					   	and DCR_NUMBER = ?
					  	and (lower(REFERENCING_TABLE_NAME) like '%sid%'
					  	or lower(REFERENCING_TABLE_NAME) like '%star%'
					  	or lower(REFERENCING_TABLE_NAME) like '%approach%')
					""";

			query = """
					select count(*) from ref_table
					  where data_supplier = ?
					  and processing_cycle = ?
					  and DCR_NUMBER = ?
					  and lower(REFERENCING_TABLE_NAME) not like '%sid%'
					  and lower(REFERENCING_TABLE_NAME) not like '%star%'
					  and lower(REFERENCING_TABLE_NAME) not like '%approach%'
					""";
			rec = app.selectInto(query, global.getDataSupplier(), pCycle, pDcr);
			vCount = rec.getInt();
			if (vCount > 0) {

				return "no";

			}

			else {
				List<Record> records = app.executeQuery(getRefInfo, global.getDataSupplier(), pCycle, pDcr);

				for (Record getRef : records) {

					vReferencingTable = toString(getRef.getObject());

					if (like("%sid%", vReferencingTable)) {
						vType = "sid";

					}

					else if (like("%star%", vReferencingTable)) {
						vType = "star";

					}

					if (like("%approach%", vReferencingTable)) {
						vStmt = "select 'Y' from " + vReferencingTable + " " + "where data_Supplier = '"
								+ global.getDataSupplier() + "' " + "and   processing_cycle = '" + pCycle + "' "
								+ "and 'RW'||approach_ident = " + substr(pRunwayIdent, 1, 4) + "B' "
								+ "and validate_ind in ('Y','S','H')";

					}

					else if (like("%heli%", vReferencingTable)) {
						vStmt = "select 'Y' from " + vReferencingTable + "_segment a, " + vReferencingTable + " b "
								+ "where a.data_Supplier = '" + global.getDataSupplier() + "' "
								+ "and   a.processing_cycle = '" + pCycle + "' " + "and   a.transition_ident = '"
								+ substr(pRunwayIdent, 1, 4) + "B' " + "and   b.validate_ind in ('Y','S','H') "
								+ "and   a.data_supplier = b.data_supplier "
								+ "and   a.processing_cycle = b.processing_cycle " + "and   a." + vType + "_ident = b."
								+ vType + "_ident " + "and   a.heliport_ident = b.heliport_ident "
								+ "and   a.heliport_icao = b.heliport_icao";

					}

					else {
						vStmt = "select 'Y' from " + vReferencingTable + "_segment a, " + vReferencingTable + " b "
								+ "where a.data_Supplier = '" + global.getDataSupplier() + "' "
								+ "and   a.processing_cycle = '" + pCycle + "' " + "and   a.transition_ident = '"
								+ substr(pRunwayIdent, 1, 4) + "B' " + "and   b.validate_ind in ('Y','S','H') "
								+ "and   a.data_supplier = b.data_supplier "
								+ "and   a.processing_cycle = b.processing_cycle " + "and   a." + vType + "_ident = b."
								+ vType + "_ident " + "and   a.airport_ident = b.airport_ident "
								+ "and   a.airport_icao = b.airport_icao";

					}
					// TODO v_B_runway := forms_utilities.get_statement_result(v_stmt)

					String dbCall = app.executeFunction(String.class, "CPTS", "get_statement_result", "forms_utilities",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_stmt", vStmt, OracleTypes.VARCHAR));
					vBRunway = dbCall;

					if (Objects.equals(vBRunway, "Y")) {
						if (Objects.equals(vCount, 0)) {
							if (Objects.equals(parameter.getRecordType(), "S")) {

								query = """
										select count(*) from pl_std_runway
																where data_supplier = ?
																and processing_cycle = ?
																and runway_ident like ?||'%'
																and runway_ident != ?
																and airport_ident = ?
																and airport_icao = ?
										""";
								rec = app.selectInto(query, global.getDataSupplier(), pCycle,
										substr(pRunwayIdent, 1, 4),
										pRunwayIdent, vAirportIdent, vAirportIcao);
								vCount = rec.getInt();

							} else {

								query = """
										select count(*) from pl_tld_runway
																where data_supplier = ?
																and processing_cycle = ?
																and customer_ident = ?
																and runway_ident like ?||'%'
																and runway_ident != ?
																and airport_ident = ?
																and airport_icao = ?
										""";
								rec = app.selectInto(query, global.getDataSupplier(), pCycle, pCust,
										substr(pRunwayIdent, 1, 4),
										pRunwayIdent, vAirportIdent, vAirportIcao);
								vCount = rec.getInt();

							}
							if (vCount > 1) {
							} else {
								return "no";
							}
						}
					} else {

						return "no";
					}
				}
				// TODO closeget_ref_info
			}

			log.info("checkRunwayRef Executed Successfully");
			return "ok";

		} catch (Exception e) {
			log.error("Error while executing checkRunwayRef" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void setUpdateDcr(String pBlock, CrRunway pRecord) throws Exception {
		log.info("setUpdateDcr Executing");
		//HashUtils hu = new HashUtils();
		// Record rec = null;
		try {
			String getRecCur = "";
			PlStdRunwayMr rstdRunway = new PlStdRunwayMr();
			PlTldRunwayMr rtldRunway = new PlTldRunwayMr();
log.debug("The default value is :"+ rstdRunway);
			Integer vDcr = toInteger(
					nameIn(this, pBlock + ".CREATE_DCR_NUMBER"));
			String vCycleData = substr(toChar(
					nameIn(this, pBlock + ".PROCESSING_CYCLE")),
					3);
			Integer vCount = 0;

			if (like("PL_STD%", pBlock)) {
				// TODO openget_rec_curforselect*frompl_std_runwaywherecreate_dcr_number=v_dcr
				getRecCur = """
						select * from pl_std_runway
							           where create_dcr_number = ?
						""";
				List<Record> cur = app.executeQuery(getRecCur, vDcr);

				for (Record getRec : cur) {
					// TODO fetchget_rec_curintorstd_runway
					rstdRunway = app.mapResultSetToClass(getRec, PlStdRunwayMr.class);
					// break;
					vCount = vCount + 1;
					if (!Objects.equals(nvl(pRecord.getCustAreaCode(), "-"), nvl(rstdRunway.getAreaCode(), "-"))
							|| !Objects.equals(nvl(pRecord.getDisplacedThresholdDistance(), 0),
									nvl(lpad(toChar(rstdRunway.getDisplacedThresholdDistance()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getLandingThresholdElevation(), 0),
									nvl(rstdRunway.getLandingThresholdElevation(), 0))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsClass(), "-"),
									nvl(rstdRunway.getLocalizerMlsClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsGlsIdent(), "-"),
									nvl(rstdRunway.getLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayDescription(), "-"),
									nvl(rstdRunway.getRunwayDescription(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayGradient(), "-"),
									nvl(rpad(rstdRunway.getRunwayGradient(), 5, " "), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLatitude(), "-"),
									nvl(rstdRunway.getRunwayLatitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLength(), 0),
									nvl(lpad(toChar(rstdRunway.getRunwayLength()), 5, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getRunwayLongitude(), "-"),
									nvl(rstdRunway.getRunwayLongitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayMagneticBearing(), "-"),
									nvl(rstdRunway.getRunwayMagneticBearing(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayWidth(), 0),
									nvl(lpad(toChar(rstdRunway.getRunwayWidth()), 3, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerClass(), "-"),
									nvl(rstdRunway.getSecondLocalizerClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerMlsGlsIdent(), "-"),
									nvl(rstdRunway.getSecondLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayAccuracyCompInd(), "-"),
									nvl(rstdRunway.getRunwayAccuracyCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getLandThreselevAccrCompInd(), "-"),
									nvl(rstdRunway.getLandThreselevAccrCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getStopway(), 0),
									nvl(lpad(toChar(rstdRunway.getStopway()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getThresholdCrossingHeight(), 0),
									nvl(lpad(toChar(rstdRunway.getThresholdCrossingHeight()), 2, '0'), 0))) {
						copy(this, toInteger(global.getDcrNumber()), pBlock + ".update_Dcr_Number");
						copy(this, vCycleData, pBlock + ".cycle_Data");

					}
				}
				//// TODO closeget_rec_cur
				//
			}

			else if (like("PL_TLD%", pBlock)) {
				// TODO openget_rec_curforselect*frompl_tld_runwaywherecreate_dcr_number=v_dcr
				getRecCur = """
						select * from pl_tld_runway
							           where create_dcr_number = ?
						""";
				List<Record> cur = app.executeQuery(getRecCur, vDcr);

				for (Record getRec : cur) {
					// TODO fetchget_rec_curintorstd_runway
					rtldRunway = app.mapResultSetToClass(getRec, PlTldRunwayMr.class);

					vCount = vCount + 1;
					if (!Objects.equals(nvl(pRecord.getDisplacedThresholdDistance(), 0),
							nvl(lpad(toChar(rtldRunway.getDisplacedThresholdDistance()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getLandingThresholdElevation(), 0),
									nvl(rtldRunway.getLandingThresholdElevation(), 0))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsClass(), "-"),
									nvl(rtldRunway.getLocalizerMlsClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsGlsIdent(), "-"),
									nvl(rtldRunway.getLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayDescription(), "-"),
									nvl(rtldRunway.getRunwayDescription(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayGradient(), "-"),
									nvl(rpad(rtldRunway.getRunwayGradient(), 5, " "), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLatitude(), "-"),
									nvl(rtldRunway.getRunwayLatitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLength(), 0),
									nvl(lpad(toChar(rtldRunway.getRunwayLength()), 5, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getRunwayLongitude(), "-"),
									nvl(rtldRunway.getRunwayLongitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayMagneticBearing(), "-"),
									nvl(rtldRunway.getRunwayMagneticBearing(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayWidth(), 0),
									nvl(lpad(toChar(rtldRunway.getRunwayWidth()), 3, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerClass(), "-"),
									nvl(rtldRunway.getSecondLocalizerClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerMlsGlsIdent(), "-"),
									nvl(rtldRunway.getSecondLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayAccuracyCompInd(), "-"),
									nvl(rtldRunway.getRunwayAccuracyCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getLandThreselevAccrCompInd(), "-"),
									nvl(rtldRunway.getLandThreselevAccrCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getStopway(), 0),
									nvl(lpad(toChar(rtldRunway.getStopway()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getThresholdCrossingHeight(), 0),
									nvl(lpad(toChar(rtldRunway.getThresholdCrossingHeight()), 2, '0'), 0))) {
						copy(this, toInteger(global.getDcrNumber()), pBlock + ".update_Dcr_Number");
						copy(this, vCycleData, pBlock + ".cycle_Data");

					}

				}
				//// TODO closeget_rec_cur
				//
			}

			if (Objects.equals(vCount, 0)) {

				copy(this, toInteger(global.getDcrNumber()), pBlock + ".update_Dcr_Number");
				copy(this, vCycleData, pBlock + ".cycle_Data");

			}

			log.info("setUpdateDcr Executed Successfully");
		}

		catch (Exception e) {
			log.error("Error while executing setUpdateDcr" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void deleteDcrNo() throws Exception {
		log.info("deleteDcrNo Executing");
		try {
			Integer lsLength = null;
			String lsDcr1 = null;
			String lsDcr2 = null;

			lsLength = instr("," + global.getNewDcrNo() + ",",
					"," + plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber() + ",");
			if (Objects.equals(lsLength, 1)) {
				lsDcr1 = null;

			}

			else {
				lsDcr1 = rtrim(ltrim(substr(global.getNewDcrNo(), 1, lsLength - 2), ", "), ", ");
			}
			lsDcr2 = rtrim(ltrim(
					substr(global.getNewDcrNo(),
							lsLength + 1 + length(
									plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())),
					", "), ", ");

			global.setNewDcrNo(ltrim(getNullClean(lsDcr1) + "," + getNullClean(lsDcr2), ", "));
			global.setNewDcrNo(ltrim(global.getNewDcrNo(), ","));
			log.info("deleteDcrNo Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing deleteDcrNo" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> onMessage(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onMessage Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// Integer msgnum = messageCode;
			// String msgtxt = messageText;
			// String msgtyp = messageType;

			// TODO Set_Application_Property(cursor_style,"DEFAULT");
			// if((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) ||
			// Objects.equals(msgnum, 40407))) {

			// TODO CLEAR_MESSAGE;
			// clearMessage();
			// parameter.setUpdRec("N");
			// setBlockProperty(toChar(nameIn(this,"system.cursor_block")),
			// FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_TRUE);
			// message("record has been saved successfully");
			//
			// }
			//
			//
			// else if(Arrays.asList(41051,40350,47316,40353).contains(msgnum)) {
			// null;
			//
			// }
			//
			//
			// else if(Objects.equals(msgnum, 41050) &&
			// Objects.equals(parameter.getWorkType(), "VIEW")) {
			// null;
			//
			// }
			//
			//
			// else if(Arrays.asList(40401,40405).contains(msgnum)) {
			// null;
			//
			// }
			//
			//
			// else if(Objects.equals(msgnum, 40352)) {
			// message("last record retrieved.");
			//
			// }
			//
			//
			// else {
			//
			// //TODO display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
			// "||msgtxt);
			// throw new FormTriggerFailureException();
			//
			// }
			// OracleHelpers.ResponseMapper(this, resDto);
			log.info(" onMessage executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the onMessage Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> onError(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onError Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer msgnum = global.getErrorCode();// errorCode;
			String msgtxt = "";// errorText;
			// String msgtyp = null;// errorType;
			String vBlockName = system.getCursorBlock();

			// TODO Set_Application_Property(cursor_style,"DEFAULT");
			if ((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) || Objects.equals(msgnum, 40407))) {
				message("changes saved successfully");

			}

			else if (Arrays.asList(41051, 40350, 47316, 40353).contains(msgnum)) {
				// null;
			}

			else if (Objects.equals(msgnum, 41050) && Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;
			}

			else if (Arrays.asList(40401, 40405).contains(msgnum)) {
				// null;
			}

			else if (Objects.equals(msgnum, 40100)) {

				// TODO clear_message;
				clearMessage();
				message("at the first record.");
			}

			else if (Objects.equals(msgnum, 40735) && like("%01031%", msgtxt)) {

				// TODO clear_message;
				clearMessage();
				coreptLib.dspMsg(msgtxt + " Insufficient privileges. ");
				// TODO dsp_msg(msgtxt||" Insufficient privileges. ");

			}

			else if (Arrays.asList(40508, 40509).contains(msgnum)) {

				// TODO dsp_msg(msgtxt||chr(10)||chr(10)||"Please check the exact error message
				// from the "Display Error" in the "HELP" menu");

			}

			else if (Arrays.asList(40200).contains(msgnum)) {
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
						if (Objects.equals(parameter.getRecordType(), "T")) {

							// TODO
							// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,v_block_name||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
							coreptLib.dspActionMsg("U", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcr_number")),
									toInteger(nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")),
											global.getProcessingCycle())),
									toChar(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));
						}

						else {

							// TODO
							// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,v_block_name||".processing_cycle"),global.getProcessingCycle()));
							coreptLib.dspActionMsg("U", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcr_number")),
									toInteger(nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")),
											global.getProcessingCycle())),
									null);

						}

					}

					else {

						// TODO dsp_msg(msgtxt);
						coreptLib.dspMsg(msgtxt.equals("")? "Field is protected against update." : msgtxt);
						throw new FormTriggerFailureException();

					}

				}

				else {

					// TODO dsp_msg(msgtxt);
					coreptLib.dspMsg(msgtxt);
					throw new FormTriggerFailureException();

				}

			}

			else if (Objects.equals(msgnum, 41050) && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					// null;

				}

				else {

					// TODO dsp_msg(msgtxt);
					coreptLib.dspMsg(msgtxt);
					throw new FormTriggerFailureException();

				}

			}

			else {

				// TODO display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
				// "||msgtxt);

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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenNewFormInstance(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenNewFormInstance Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Initialize_Form --- Program Unit Calling
			OracleHelpers.bulkClassMapper(this, coreptTemplate);
			coreptTemplate.initializeForm();
			global.setNewDcrNo("");
			// hideView("stdTldFixStk");

			// TODO set_block;
			coreptLib.setBlock();
			// TODO if_from_error_summary;
			coreptLib.iffromerrorsummary();
			RecordGroup groupId = null;
			// RecordGroupColumn colId = null;
			String vGroup = "newDcr";

			groupId = findGroup("newDcr");
			if (groupId != null) {
				deleteGroup(groups, "newDcr");
			}

			else {
				groupId = createGroup(vGroup);
				// colId = addGroupColumn(groupId, "dcrNo", "numberColumn");

				// coverity-fixes
				addGroupColumn(groupId, "dcrNo", "numberColumn");
			}

			String queryHits = toString(plTldRunwayMr.getQueryHits());
			OracleHelpers.ResponseMapper(this, reqDto);
			whenNewRecordInstance(reqDto);
			plTldRunwayMr.setQueryHits(queryHits);
			if (!system.getFormStatus().equals("NEW")) {
              global.setCreateDcrNumber(toString(
                      nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
          }
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenNewRecordInstance(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenNewRecordInstance Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			String pBlock = system.getCursorBlock();
			Integer vAllowUpdate = 0;
			OracleHelpers.bulkClassMapper(this, coreptLib);
			if (like("PL_%", pBlock)) {
				if (Objects.equals(system.getMode(), "NORMAL")
						&& !Objects.equals(nvl(toChar(nameIn(this, pBlock + ".VALIDATE_IND")), "N"), "Y")
						&& !Objects.equals(toChar(nameIn(this, pBlock + ".PROCESSING_CYCLE")), null)) {

					// TODO do_validate(p_block) --- Program Unit Calling
					doValidate(pBlock, "Y");

				} else {
					OracleHelpers.bulkClassMapper(this, coreptLib);
					coreptLib.setinitialerrordisplay(pBlock);
				}
				if (!Objects.equals(parameter.getWorkType(), "VIEW")
						&& Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "N")) {
						setBlockItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						if (!like("%MR", pBlock)) {
							setBlockItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "notUpdatable");
							setBlockItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute",
									"notUpdatable");
							setBlockItemProperty(pBlock + ".validate_ind", "current_record_attribute", "default");
						}

					}

					else {
						setBlockItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						if (!like("%MR", pBlock)) {
							setBlockItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute", "default");
							setBlockItemProperty(pBlock + ".validate_ind", "current_record_attribute", "notUpdatable");
						}

						if (Objects.equals(global.getLibRefreshed(), "Y")
								&& Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "Y")) {
							setBlockItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
									FormConstant.PROPERTY_FALSE);
							if (!like("%MR", pBlock)) {
								setBlockItemProperty(pBlock + ".processing_cycle", "current_record_attribute",
										"notUpdatable");
							}
						} else {
							setBlockItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
									FormConstant.PROPERTY_TRUE);
							if (!like("%MR", pBlock)) {
								setBlockItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "default");

							}

						}

					}

				}

				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(toChar(nameIn(this, pBlock + ".processing_Cycle")), null)) {
						vAllowUpdate = toInteger(coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, pBlock + ".customer_Ident"))));

					} else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, pBlock + ".processing_Cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, pBlock + ".customer_Ident")));

					}
				} else {
					if (Objects.equals(nameIn(this, pBlock + ".processing_Cycle"), null)) {
						vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);

					} else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, pBlock + ".processing_Cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);
					}

				}
				if (Objects.equals(vAllowUpdate, 1)) {
					parameter.setUpdRec("N");
					setBlockProperty(pBlock, FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_TRUE);

				} else {
					parameter.setUpdRec("Y");
					setBlockProperty(pBlock, FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_FALSE);
					if (!Objects.equals(toChar(nameIn(this, pBlock + ".processing_Cycle")),
							global.getProcessingCycle())) {
						throw new FormTriggerFailureException(event);

					}

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyExeqry(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExeqry Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				checkToCommit("EXECUTE_QUERY", reqDto);
				system.setFormStatus("NORMAL");
				if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR"))
					this.getPlStdRunwayMr().add(0, new PlStdRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR"))
					this.getPlTldRunwayMr().add(0, new PlTldRunwayMr());
				system.setCursorRecordIndex(0);
			}
			else if (Objects.equals(system.getMode(), "NORMAL") && Objects.equals(parameter.getWorkType(), "VIEW")) {
				system.setFormStatus("NORMAL");
				if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR"))
					this.getPlStdRunwayMr().add(0, new PlStdRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR"))
					this.getPlTldRunwayMr().add(0, new PlTldRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "STD_RUNWAY_MR"))
					this.getStdRunwayMr().add(0, new StdRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "TLD_RUNWAY_MR"))
					this.getTldRunwayMr().add(0, new TldRunwayMr());
				system.setCursorRecordIndex(0);
			}

			preQueryExecute();

			controlBlock.setChkUnchkAll("N");
			global.setNewDcrNo("");
			
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyExeqry executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyExeqry Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	private void preQueryExecute() throws Exception {

		if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR")) {
			PlStdRunwayMrPreQuery();
		} else if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR")) {
			PlTldRunwayMrPreQuery();
		} else if (Objects.equals(system.getCursorBlock(), "TLD_RUNWAY_MR")) {
			tldRunwayMrPreQuery();
		} else if (Objects.equals(system.getCursorBlock(), "STD_RUNWAY_MR")) {
			stdRunwayMrPreQuery();
		}

		coreptLib.coreptexecutequery(this);

		if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR")) {
			for (PlTldRunwayMr PlTldRunwayMr : plTldRunwayMr.getData()) {
				PlTldRunwayMr.setOldProcessingCycle(toInteger(PlTldRunwayMr.getProcessingCycle()));
			}
		}

		if (!system.getFormStatus().equals("NEW")) {
			global.setCreateDcrNumber(toString(nameIn(this,
					HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
		}

	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyEntqry(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyEntqry Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {

				// TODO check_to_commit('ENTER_QUERY') --- Program Unit Calling
				checkToCommit("ENTER_QUERY", reqDto);

			}

			coreptLib.coreptenterquery();
			system.setMode("ENTER_QUERY");
			system.setFormStatus("NORMAL");
			controlBlock.setChkUnchkAll("N");
			global.setNewDcrNo("");
			if (Objects.equals(system.getMode(), "NORMAL")) {

				// TODO unset_query_menu_items;
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> preInsert(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" preInsert Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String query = "";
			Record rec = null;
			String vValidateInd = null;
			Integer vDcrNumber = null;
			Integer vProcessingCycle = null;
			String vBlockName = toSnakeCase(system.getCursorBlock());
			Integer vAllowUpdate = 0;

			coreptLib.checkwildcardforkeys(vBlockName);

			if (Arrays.asList("CHANGED", "INSERT").contains(system.getRecordStatus())) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_Cycle"), null)) {
						vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, vBlockName + ".customer_ident")));

					}

					else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, vBlockName + ".processing_cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, vBlockName + ".customer_ident")));

					}

				}

				else {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);

					}

					else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, vBlockName + ".processing_cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);

					}

				}

			}

			if (Objects.equals(vAllowUpdate, 1)) {
				copy(this, global.getDataSupplier(), vBlockName + ".data_Supplier");
				copy(this, 0, vBlockName + ".file_Recno");

				query = """
						select dcr_number_seq.nextval from dual
						""";
				rec = app.selectInto(query);
				vDcrNumber = rec.getInt();
				copy(this, vDcrNumber, vBlockName + ".create_Dcr_Number");

				doValidate(vBlockName, "N");
				vValidateInd = toChar(nameIn(this, vBlockName + ".validate_ind"));
				vProcessingCycle = toInteger(
						nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")), global.getProcessingCycle()));
				if (Objects.equals(global.getLibRefreshed(), "Y")
						&& Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle()).contains(
								toChar(toChar(vProcessingCycle)))
						&& Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {

					refreshMasterLibrary.setRecordGroup(vDcrNumber, vValidateInd, vBlockName,
							vProcessingCycle, "I");
				}

			} else {
				if (Objects.equals(parameter.getRecordType(), "T")) {

					coreptLib.dspActionMsg("I", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, vBlockName + ".processing_cycle"), global.getProcessingCycle())),
							toChar(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));
				} else {
					coreptLib.dspActionMsg("I", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")),
									global.getProcessingCycle())),
							null);
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" preInsert executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the preInsert Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> preUpdate(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" preUpdate Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vBlockName = system.getCursorBlock();
			Integer vDcrNumber = toInteger(nameIn(this, vBlockName + ".create_dcr_number"));
			Integer vProcessingCycle = toInteger(nameIn(this, vBlockName + ".processing_cycle"));
			String vTable = toChar(nameIn(this, vBlockName + ".query_Data_Source_Name"));
			String vValidateIndNew = null;
			String vValidateIndOld = null;

			// TODO v_validate_ind_old :=
			// refresh_ml_utilities.get_validate_ind(v_table,v_dcr_number)
			String dbCall = app.executeFunction(String.class, "CPTM", "Get_validate_ind", "refresh_ml_utilities",
					OracleTypes.VARCHAR,
					new ProcedureInParameter("p_table", vTable, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_dcr", vDcrNumber, OracleTypes.NUMBER));
			vValidateIndOld = dbCall;

			if (Arrays.asList("Y", "S", "H", "O").contains(vValidateIndOld)) {

				refreshMasterLibrary.deleteFromRefTable(vDcrNumber, null);
			}

			doValidate(vBlockName, "N");
			vValidateIndNew = toChar(nameIn(this, vBlockName + ".validate_ind"));
			if (Arrays.asList("Y", "S", "H", "O").contains(vValidateIndOld)
					&& Arrays.asList("W", "N", "I").contains(vValidateIndNew)) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(
							toChar(checkRunwayRef("pl_tld_runway", toChar(nameIn(this, vBlockName + ".runway_ident")),
									toInteger(nameIn(this, vBlockName + ".processing_cycle")),
									toChar(nameIn(this, vBlockName + ".customer_ident")),
									toChar(nameIn(this, vBlockName + ".create_dcr_number")))),
							"no")) {
						if (Objects.equals(toChar(refreshMasterLibrary.checkReferenceInfo(vBlockName, null)), "N")) {
							throw new FormTriggerFailureException(event);
						}
					}
				}

				else {
					if (Objects.equals(
							toChar(checkRunwayRef("pl_std_runway", toChar(nameIn(this, vBlockName + ".runway_ident")),
									toInteger(nameIn(this, vBlockName + ".processing_cycle")), null,
									toChar(nameIn(this, vBlockName + ".create_dcr_number")))),
							"no")) {
						if (Objects.equals(toChar(refreshMasterLibrary.checkReferenceInfo(vBlockName, null)), "N")) {
							throw new FormTriggerFailureException(event);
						}
					}
				}
			}

			if (Objects.equals(global.getLibRefreshed(), "Y")
					&& Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle())
							.contains(toChar(vProcessingCycle))) {
				if (Arrays.asList("Y", "S", "H", "O").contains(vValidateIndNew)
						|| Arrays.asList("Y", "S", "H", "O").contains(vValidateIndOld)) {

					refreshMasterLibrary.setRecordGroup(vDcrNumber, vValidateIndNew, vBlockName, vProcessingCycle, "U");
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" preUpdate executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the preUpdate Service");
			throw e;
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyDelrec(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDelrec Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		Boolean isChecked = false;
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vAllowUpdate = 0;
			String lsBlockName = system.getCursorBlock();
			// Object obj =
			// OracleHelpers.findBlock(this,HoneyWellUtils.toCamelCase(system.getCursorBlock()));
			// Integer lnButton = 0;
			Integer lnDcrNo = null;
			String lsTableName = null;
			String lsFixIdent = null;
			String lsAirportIdent = null;
			String lsAirportIcao = null;
			Integer lnProcessingCycle = null;
			Integer vButton = 0;
			String lsRefInfo = null;

			lsFixIdent = toChar(nameIn(this, lsBlockName + ".runway_Ident"));
			lsAirportIdent = toChar(nameIn(this, lsBlockName + ".airport_Ident"));
			lsAirportIcao = toChar(nameIn(this, lsBlockName + ".airport_Icao"));
			lnProcessingCycle = toInteger(nameIn(this, lsBlockName + ".processing_Cycle"));
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			} else {
				if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					if (Objects.equals(parameter.getRecordType(), "T")) {
						if (Objects.equals(nameIn(this, lsBlockName + ".processing_Cycle"), null)) {
							vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(),
									toChar(nameIn(this, lsBlockName + ".customer_Ident")));

						} else {
							vAllowUpdate = coreptLib.checkValidSuppCust(
									toInteger(nameIn(this, lsBlockName + ".processing_Cycle")),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(),
									toChar(nameIn(this, lsBlockName + ".customer_Ident")));

						}
					} else {
						if (Objects.equals(nameIn(this, lsBlockName + ".processing_Cycle"), null)) {
							vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(), null);

						} else {
							vAllowUpdate = coreptLib.checkValidSuppCust(
									toInteger(nameIn(this, lsBlockName + ".processing_Cycle")),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(), null);

						}
					}
					if (Objects.equals(vAllowUpdate, 1)) {
						if(!Arrays.asList("NEW","INSERT").contains(system.getRecordStatus())) {
						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
							vButton = moreButtons("S", "Delete Record",
									"You are going to delete this record. Please be sure." + "\n" + " ", "Delete It",
									"Cancel", "");
							alertDetails.createNewRecord("delRec");
							throw new AlertException(event, alertDetails);
						} else {
							vButton = alertDetails.getAlertValue("delRec", alertDetails.getCurrentAlert());
						}

                        if (Objects.equals(vButton, 1)) {

                          String pTableType = "M2C";
                          String lsBlockName1 = system.getCursorBlock();
                          // String pTableName = ltrim(upper(getBlockProperty(lsBlockName,
                          // queryDataSourceName)), "PL_");
                          String pTableName = ltrim(
                              upper(toChar(nameIn(this, lsBlockName + ".query_Data_Source_Name"))),
                              "PL_");
                          Integer vDcrNumber = toInteger(
                              nameIn(this, lsBlockName1 + ".create_Dcr_Number"));
                          Integer vProcessingCycle = toInteger(
                              nameIn(this, lsBlockName1 + ".processing_Cycle"));
                          String vValidateInd = toChar(
                              nameIn(this, lsBlockName1 + ".validate_Ind"));
                          String vStatus = system.getRecordStatus();
                          String blkName = toChar(
                              nameIn(this, lsBlockName1 + ".query_Data_Source_Name"));
                          // String dcrNumber = toChar(nameIn(this, lsBlockName1 +
                          // ".create_Dcr_Number"));

                          if (Objects.equals(vStatus, "CHANGED")) {
                            // TODO v_validateInd :=
                            // refresh_ml_utilities.Get_validate_ind(get_block_property(ls_Block_Name,QUERY_DATA_SOURCE_NAME),name_in(ls_Block_Name||'.createDcrNumber'))
                            String dbCall = app.executeFunction(String.class, "CPTM",
                                "Get_validate_ind", "refresh_ml_utilities", OracleTypes.VARCHAR,
                                new ProcedureInParameter("p_table", blkName, OracleTypes.VARCHAR),
                                new ProcedureInParameter("p_dcr", vDcrNumber, OracleTypes.NUMBER));
                            vValidateInd = dbCall;

                          }

                          if (Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {
                            if (Objects.equals(parameter.getRecordType(), "T")) {
                              if (Objects
                                  .equals(
                                      toChar(checkRunwayRef("pl_tld_runway",
                                          toChar(nameIn(this, lsBlockName1 + ".runway_Ident")),
                                          toInteger(
                                              nameIn(this, lsBlockName1 + ".processing_Cycle")),
                                          toChar(nameIn(this, lsBlockName1 + ".customer_Ident")),
                                          toChar(
                                              nameIn(this, lsBlockName1 + ".create_Dcr_Number")))),
                                      "no")) {
                                if (Objects.equals(
                                    toChar(
                                        refreshMasterLibrary.checkReferenceInfo(lsBlockName1, "D")),
                                    "N")) {

                                  // TODO
                                  // Forms_Utilities.Du_Std_Tld_Fix_Prc(ls_Fix_Ident,NULL,ls_Airport_Ident,ls_Airport_Icao,"P","G",global.getDataSupplier(),ln_processingCycle,ln_Dcr_No,ls_Table_Name);

                                  Map<String, Object> dbCall = app.executeProcedure("CPTS",
                                      "Du_Std_Tld_Fix_Prc", "Forms_Utilities",
                                      new ProcedureInParameter("pi_Fix_Ident", lsFixIdent,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Fix_Icao", null,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Airport_Ident", lsAirportIdent,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Airport_Icao", lsAirportIcao,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Sec", "P", OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Sub_Sec", "G",
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Data_Supplier",
                                          global.getDataSupplier(), OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_processingCycle",
                                          lnProcessingCycle, OracleTypes.NUMBER),
                                      new ProcedureOutParameter("lnDcrNo", OracleTypes.NUMBER),
                                      new ProcedureOutParameter("lsTableName",
                                          OracleTypes.VARCHAR));

                                  BigDecimal bg = OracleHelpers.toBigDecimal(dbCall.get("lnDcrNo"));
                                  if (!Objects.equals(bg, null)) {
                                    lnDcrNo = bg.intValue();
                                  }
                                  lsTableName = toString(dbCall.get("lsTableName"));
                                  lsRefInfo = "N";
                                  // showView("stdTldFixStk");
                                  // hideView("stdTldFixStk");

                                }
                              }
                            }

                            else {
                              if (Objects
                                  .equals(
                                      toChar(checkRunwayRef("pl_std_runway",
                                          toChar(nameIn(this, lsBlockName1 + ".runway_Ident")),
                                          toInteger(
                                              nameIn(this, lsBlockName1 + ".processing_Cycle")),
                                          null,
                                          toChar(
                                              nameIn(this, lsBlockName1 + ".create_Dcr_Number")))),
                                      "no")) {
                                if (Objects.equals(
                                    toChar(
                                        refreshMasterLibrary.checkReferenceInfo(lsBlockName1, "D")),
                                    "N")) {
                                  throw new FormTriggerFailureException(event);

                                }
                              }
                            }
                          }

                          if (!Objects.equals(lnDcrNo, null)) {

                            alertDetails.getCurrent();
                            if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                              vButton = moreButtons("S", "Reference Record",
                                  "This Fix has Reference.Do You Want to Delete the Fix?" + chr(10)
                                      + " ",
                                  "Yes", "No", "");
                              alertDetails.createNewRecord("refRec");
                              throw new AlertException(event, alertDetails);
                            } else {
                              vButton = alertDetails.getAlertValue("refRec",
                                  alertDetails.getCurrentAlert());
                            }
                            if (Objects.equals(vButton, 1)) {

                              // Map<String, Object> updRec = app.executeProcedure("CPTS",
                              // "Du_Update_Ref_Table_Prc", "Forms_Utilities",
                              // new ProcedureInParameter("pi_Old_dcrNumber",
                              // toInteger(nameIn(this, lsBlockName + ".create_Dcr_Number")),
                              // OracleTypes.NUMBER),
                              // new ProcedureInParameter("pi_New_dcrNumber", lnDcrNo,
                              // OracleTypes.NUMBER),
                              // new ProcedureInParameter("pi_Table_Name", lsTableName,
                              // OracleTypes.VARCHAR));

                              // coverity-fixes
                              app.executeProcedure("CPTS", "Du_Update_Ref_Table_Prc",
                                  "Forms_Utilities",
                                  new ProcedureInParameter("pi_Old_dcrNumber",
                                      toInteger(nameIn(this, lsBlockName + ".create_Dcr_Number")),
                                      OracleTypes.NUMBER),
                                  new ProcedureInParameter("pi_New_dcrNumber", lnDcrNo,
                                      OracleTypes.NUMBER),
                                  new ProcedureInParameter("pi_Table_Name", lsTableName,
                                      OracleTypes.VARCHAR));
                            } else {
                              throw new FormTriggerFailureException(event);

                            }

                          }

                          else if (Objects.equals(lsRefInfo, "N")) {
                            throw new FormTriggerFailureException(event);

                          }

                          if (Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {

                            refreshMasterLibrary.deleteFromRefTable(vDcrNumber, null);
                          }

                          if (Objects.equals(global.getLibRefreshed(), "Y")
                              && Arrays
                                  .asList(global.getNewProcessingCycle(),
                                      global.getOldProcessingCycle())
                                  .contains(toChar(vProcessingCycle))
                              && Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {

//								refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber, lnProcessingCycle,
//										pTableName, "I", null);
//								deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
                            if (parameter.getMultiRecDel() > 0) {
                              if (system.getCursorBlock().equals("PL_TLD_RUNWAY_MR")) {
                                chkSelectAll(isChecked, nameIn(this, system.getCursorBlock()));
                                for (PlTldRunwayMr plTldRunwayMr : plTldRunwayMr.getData()) {
                                  if (Objects.equals(plTldRunwayMr.getRecordStatus(), "DELETED")
                                      && Objects.equals(plTldRunwayMr.getChk(), "Y")) {
                                    pTableName = ltrim(upper(getBlockProperty("PL_TLD_RUNWAY_MR",
                                        "Query_Data_Source_Name")), "PL_");
                                    vDcrNumber = toInteger(plTldRunwayMr.getCreateDcrNumber());
                                    vProcessingCycle = toInteger(
                                        plTldRunwayMr.getProcessingCycle());
                                    refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber,
                                        lnProcessingCycle, pTableName, "I", null);
                                  }
                                }
                                validateCommit(reqDto);
                                commitForm(this);
                                sendUpdatedRowIdDetails();
                                system.setFormStatus("NORMAL");
                                coreptLib.dspMsg(
                                    "The refresh master library table for this deletion is done \n and all changes are commited.");
                                filterNonInsertedRecords(nameIn(this, system.getCursorBlock()));
                              }
                              controlBlock.setChkUnchkAll("N");
                              parameter.setMultiRecDel(0);
                            } else {
                              refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber,
                                  lnProcessingCycle, pTableName, "I", null);
                              copy("DELETED", lsBlockName + ".record_status");

                              validateCommit(reqDto);
                              commitForm(this);
                              sendUpdatedRowIdDetails();
                              refreshMasterLibrary.setRecordGroup(vDcrNumber, "I", lsBlockName1,
                                  vProcessingCycle, "D");
                              system.setRecordStatus("DELETED");
                              system.setFormStatus("NORMAL");
                              coreptLib.dspMsg(
                                  "The refresh master library table for this deletion is done \n and all changes are commited.");
                            }
                          } else if (parameter.getMultiRecDel() > 0) {
                            if (system.getCursorBlock().equals("PL_TLD_RUNWAY_MR")) {
                              chkSelectAll(isChecked, nameIn(this, system.getCursorBlock()));
                              validateCommit(reqDto);
                              commitForm(this);
                              sendUpdatedRowIdDetails();
                              system.setFormStatus("QUERIED");
                              filterNonInsertedRecords(nameIn(this, system.getCursorBlock()));
                            }
                            controlBlock.setChkUnchkAll("N");
                            parameter.setMultiRecDel(0);
                            coreptLib.dspMsg(
                                "The refresh master library table for this deletion is done \n and all changes are commited.");
                          } else {

                            //deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
                            copy("DELETED", lsBlockName + ".record_status");
                            String _rowId = toString(nameIn(this, lsBlockName + ".rowid"));
                            sendLockRowIdDetails(_rowId);
                            system.setRecordStatus("DELETED");
                            system.setFormStatus("CHANGED");
                          }
                        } else {
							throw new FormTriggerFailureException();
						}
					}
					} else {
						if (Objects.equals(parameter.getRecordType(), "T")) {

							coreptLib.dspActionMsg("D", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcr_Number")),
									toInteger(nvl(toChar(nameIn(this, lsBlockName + ".processing_Cycle")),
											global.getProcessingCycle())),
									toChar(nameIn(this, system.getCursorBlock() + ".customer_Ident")));

						} else {

							coreptLib.dspActionMsg("D", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcrNumber")),
									toInteger(nvl(toChar(nameIn(this, lsBlockName + ".processing_Cycle")),
											global.getProcessingCycle())),
									null);

						}
					}
				} else {
					if (Arrays.asList("Y", "S", "H", "O")
							.contains(toChar(nameIn(this, system.getCursorBlock() + ".validate_Ind")))) {

						refreshMasterLibrary.deleteFromRefTable(
								toInteger(nameIn(this, system.getCursorBlock() + ".create_Dcr_Number")), null);
					}

					//deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
					copy("DELETED", lsBlockName + ".record_status");
					deleteRecord(system.getCursorBlock());
					system.setRecordStatus("DELETED");
					system.setFormStatus("CHANGED");
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyDelrec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyDelrec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenFormNavigate(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenFormNavigate Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getFormStatus(), "CHANGED")
					&& Objects.equals(toChar(nameIn(this, "global.check_save")), "Y")) {

				coreptLib.checkSavePrc("Runway");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenFormNavigate executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenFormNavigate Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void PlStdRunwayMrPreQuery() throws Exception {
		log.info("PlStdRunwayMrPreQuery Executing");
		try {
			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				plStdRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plStdRunwayMr.getRow(system.getCursorRecordIndex())
							.setProcessingCycle(global.getProcessingCycle());
				}
			}
			log.info("PlStdRunwayMrPreQuery Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing PlStdRunwayMrPreQuery" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrWhenClearBlock(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrWhenClearBlock Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			setItemProperty("control_block.std_validation_errors_mr", FormConstant.VISIBLE,
					FormConstant.PROPERTY_FALSE);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrWhenClearBlock executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrWhenClearBlock Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrProcessingCycleWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrProcessingCycleWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(rtrim(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle())),
					null)) {
				plStdRunwayMr.getRow(system.getCursorRecordIndex())
						.setProcessingCycle(global.getProcessingCycle());

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrProcessingCycleWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrProcessingCycleWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("plStdMrCntd");
			goItem("pl_std_runway_mr.airport_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("plStdMrCntd");
			goItem("pl_std_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void PlTldRunwayMrPreQuery() throws Exception {
		log.info("PlTldRunwayMrPreQuery Executing");
		try {
			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				plTldRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plTldRunwayMr.getRow(system.getCursorRecordIndex())
							.setProcessingCycle(global.getProcessingCycle());
				}

			}
		} catch (Exception e) {
			log.error("Error while executing PlTldRunwayMrPreQuery" + e.getMessage());
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrWhenClearBlock(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrWhenClearBlock Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			setItemProperty("control_block.tld_validation_errors_mr", FormConstant.VISIBLE,
					FormConstant.PROPERTY_FALSE);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrWhenClearBlock executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrWhenClearBlock Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrPostQuery(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" PlTldRunwayMrPostQuery Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			for (PlTldRunwayMr PlTldRunwayMr : plTldRunwayMr.getData()) {
				PlTldRunwayMr.setOldProcessingCycle(toInteger(PlTldRunwayMr.getProcessingCycle()));
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrPostQuery executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrCustomerIdentWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrCustomerIdentWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vFlag = 0;
			Integer vAllowUpdate = 0;
			String vBlockName = system.getCursorBlock();

			// vAllowUpdate = 0;
			if (Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(global.getProcessingCycle()), global.getDataSupplier(),
								parameter.getRecordType(), toChar(nameIn(this, vBlockName + ".customer_ident")));

					} else {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(nameIn(this, vBlockName + ".processing_cycle")), global.getDataSupplier(),
								parameter.getRecordType(), toChar(nameIn(this, vBlockName + ".customer_ident")));

					}

				} else {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(global.getProcessingCycle()), global.getDataSupplier(),
								parameter.getRecordType(), null);

					} else {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(nameIn(this, vBlockName + ".processing_cycle")), global.getDataSupplier(),
								parameter.getRecordType(), null);

					}

				}
				if (Arrays.asList("J", "L", "E").contains(global.getDataSupplier())) {
					if (Arrays.asList(6, 4, 3, 2, 1).contains(vFlag)) {
						vAllowUpdate = 1;

					} else if (Objects.equals(vFlag, 0)) {
						if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {

							coreptLib.dspMsg("Customer is not associated with DCR# " + global.getDcrNumber()
									+ " Or with \n processing cycle " + global.getProcessingCycle());

						} else {

							coreptLib.dspMsg("Customer is not associated with DCR# " + global.getDcrNumber()
									+ " Or with \n processing cycle "
									+ toChar(nameIn(this, vBlockName + ".processing_cycle")));

						}
						throw new FormTriggerFailureException(event);
					}

				}

				else if (Arrays.asList("Q", "N").contains(global.getDataSupplier())) {
					if (Objects.equals(vFlag, 5)) {
						vAllowUpdate = 1;

					}
				}
				log.info("vall" + vAllowUpdate);
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrCustomerIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrCustomerIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrProcessingCycleWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldRunwayMrProcessingCycleWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle(), null)) {
				plTldRunwayMr.getRow(system.getCursorRecordIndex())
						.setProcessingCycle(global.getProcessingCycle());

			}

			if (!Objects.equals(system.getRecordStatus(), "NEW")) {
				if (!Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle(),
					toString(	plTldRunwayMr.getRow(system.getCursorRecordIndex()).getOldProcessingCycle()))) {
					if (Objects.equals(
							toChar(checkRunwayRef("pl_tld_runway",
									plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent(),
									toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()),
									plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCustomerIdent(),
									toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()))),
							"no")) {
						if (Objects.equals(toChar(refreshMasterLibrary.checkReferenceInfo("PL_TLD_RUNWAY_MR", "P")),
								"N")) {

							// TODO pause;
							plTldRunwayMr.getRow(system.getCursorRecordIndex()).setProcessingCycle(
									toString(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getOldProcessingCycle()));

						}

					}

				}

			}
			plTldRunwayMr.getRow(system.getCursorRecordIndex())
					.setOldProcessingCycle(toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()));

			OracleHelpers.ResponseMapper(this, reqDto);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrProcessingCycleWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrProcessingCycleWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrGeneratedInHouseFlagWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrGeneratedInHouseFlagWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getGeneratedInHouseFlag(),
						null)) {
					plTldRunwayMr.getRow(system.getCursorRecordIndex()).setGeneratedInHouseFlag("Y");

				}
			}

			OracleHelpers.ResponseMapper(this, reqDto);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrGeneratedInHouseFlagWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrGeneratedInHouseFlagWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("plTldMrCntd");
			goItem("pl_tld_runway_mr.customer_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("plTldMrCntd");
			goItem("pl_tld_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrChkWhenCheckboxChanged(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrChkWhenCheckboxChanged Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
				parameter.setMultiRecDel(parameter.getMultiRecDel() + 1);

			}

			else if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "N")) {
				parameter.setMultiRecDel(parameter.getMultiRecDel() - 1);

			}

			String lsGroup = "NEW_DCR";
			RecordGroup groupId = findGroup(lsGroup);
			Integer lnRow = getGroupRowCount(groupId);
			// TODO Object colId = findColumn("NEW_DCR.DCR_NO");

			Number colVal = null;

			if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
				addGroupRow(groupId, "end_of_group");
				// lnRow = lnRow + 1;

				setGroupNumberCell(groupId, "dcrNo", lnRow,
						toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				// setgroup_number_cell("NEW_DCR.DCR_NO",ln_row,plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber());
				global.setNewDcrNo(
						rtrim(ltrim(
								rtrim(global.getNewDcrNo(), ", ") + ","
										+ plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber(),
								", "), ", "));

			} else if (Objects.equals(nvl(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "N"), "N")) {
				for (int i = 0; i < lnRow; i++) {
					// TODO colVal = getGroupNumberCell(colId,i);
					colVal = getGroupNumberCell("newDcr.dcrNo", i);
					if (Objects.equals(toInteger(  colVal),
						toInteger(	plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()))) {
						deleteGroupRow("newDcr", i);
						break;
					}
				}

				// TODO delete_dcr_no --- Program Unit Calling
				deleteDcrNo();
			}
			global.setNewDcrNo(ltrim(global.getNewDcrNo(), ","));
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrChkWhenCheckboxChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrChkWhenCheckboxChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void stdRunwayMrPreQuery() throws Exception {
		log.info("stdRunwayMrPreQuery Executing");
		try {
			stdRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
			log.info("stdRunwayMrPreQuery Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing stdRunwayMrPreQuery" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> stdRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("stdMrCntd");
			goItem("std_runway_mr.airport_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> stdRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("stdMrCntd");
			goItem("std_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void tldRunwayMrPreQuery() throws Exception {
		log.info("tldRunwayMrPreQuery Executing");
		try {
			tldRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
			log.info("tldRunwayMrPreQuery Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing tldRunwayMrPreQuery" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> tldRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" tldRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("tldMrCntd");
			goItem("tld_runway_mr.customer_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> tldRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" tldRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("tldMrCntd");
			goItem("tld_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockTldOverideMrWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockTldOverideMrWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vItem = system.getCursorItem();

			parameter.setTldOverrideButtonFlg("MR");
			showView("tldOver");
			goItem("control_block.tld_override_errors");
			goItem(vItem);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockTldOverideMrWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockTldOverideMrWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockStdOverideMrWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockStdOverideMrWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vItem = system.getCursorItem();

			parameter.setStdOverrideButtonFlg("MR");
			showView("stdOver");
			goItem("control_block.std_override_errors");
			goItem(vItem);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockStdOverideMrWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockStdOverideMrWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockCloseTldOverrideWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockCloseTldOverrideWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("tldOver");
			if (Objects.equals(parameter.getTldOverrideButtonFlg(), "MR")) {
				goItem("pl_tld_runway_mr.customer_ident");

			}

			else if (Objects.equals(parameter.getTldOverrideButtonFlg(), "SR")) {
				goItem("pl_tld_runway_sr.customer_ident");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockCloseTldOverrideWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockCloseTldOverrideWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockCloseStdOverrideWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockCloseStdOverrideWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("stdOver");
			if (Objects.equals(parameter.getStdOverrideButtonFlg(), "MR")) {
				goItem("pl_std_runway_mr.airport_ident");

			}

			else if (Objects.equals(parameter.getStdOverrideButtonFlg(), "SR")) {
				goItem("pl_std_runway_sr.airport_ident");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockCloseStdOverrideWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockCloseStdOverrideWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockChkUnchkAllWhenCheckboxChanged(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockChkUnchkAllWhenCheckboxChanged Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			RecordGroup groupId = findGroup("newDcr");
			Integer lnRow = getGroupRowCount(groupId);
			Number colVal = null;
			// Integer lnRow1 = 0;

			if (Objects.equals(controlBlock.getChkUnchkAll(), "Y")) {
				if (findGroup("newDcr") != null) {
					deleteGroupRow("newDcr", "ALL_ROWS");

				}

				for (int i = 1; i <= lnRow; i++) {

					// TODO delete_dcr_no --- Program Unit Calling
					deleteDcrNo();

				}
				lnRow = getGroupRowCount(groupId);
				goBlock("PlTldRunwayMr", "");

				// TODO First_record;
				for (PlTldRunwayMr PlTldRunwayMr : plTldRunwayMr.getData()) {

					PlTldRunwayMr.setChk("Y");
					parameter.setMultiRecDel(parameter.getMultiRecDel() + 1);
					addGroupRow(groupId, "end_of_group");
					//

					// set_group_number_cell("NEW_DCR.DCR_NO",ln_row,PlTldRunwayMr.getCreateDcrNumber());
					setGroupNumberCell(groupId, "dcrNo", lnRow,
							toInteger(PlTldRunwayMr.getCreateDcrNumber()));
					global.setNewDcrNo(rtrim(ltrim(
							global.getNewDcrNo() + "," + PlTldRunwayMr.getCreateDcrNumber(), ", "),
							", "));
					lnRow = lnRow + 1;
					if (!Objects.equals(PlTldRunwayMr.getCustomerIdent(), null)) {
						// nextRecord("");

					} else {
						// clearRecord("");
						PlTldRunwayMr.setChk("Y");
						break;
					}
				}
				// TODO First_record;
			} else {
				parameter.setMultiRecDel(0);
				lnRow = getGroupRowCount(groupId) - 1;
				goBlock("PlTldRunwayMr", "");

				// TODO First_record;
				system.setCursorRecordIndex(0);
				for (int i = 1; i <= plTldRunwayMr.size(); i++) {

					if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).setChk("N");
						for (int x = 0; x < lnRow; x++) {
							colVal = getGroupNumberCell("newDcr.dcrNo", x);
							if (Objects.equals(toInteger( colVal),
								toInteger(	plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())) ){
								deleteGroupRow("newDcr", x);
								break;
							}
						}
					}
					// TODO delete_dcr_no --- Program Unit Calling
					deleteDcrNo();
					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
				}
				// TODO First_record;
			}
			global.setNewDcrNo(ltrim(global.getNewDcrNo(), ","));
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockChkUnchkAllWhenCheckboxChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockChkUnchkAllWhenCheckboxChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyCommit(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCommit Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (global.getClearBlock()) {
				validateCommit(reqDto);
				try {
					commitForm(this);
					sendUpdatedRowIdDetails();
					coreptLib.message("Record has been saved successfully");
				} catch (Exception e) {
					if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
							|| e instanceof EntityExistsException
							|| e.getCause() instanceof ConstraintViolationException
							|| e.getMessage().contains("ORA-00001")) {
						global.setErrorCode(501);

						coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
								+ "error message from the \"Display \n Error\" in the \"HELP\" menu");

						log.info(" Unique Constrain Error while Executing the keyCommit Service");
					} else {
						throw e;
					}
				}
				global.setClearBlock(false);
			} else {
				if (Objects.equals(parameter.getWorkType(), "VIEW")) {
					// null
				} else {

					checkToCommit("COMMIT", reqDto);
					system.setMode("NORMAL");
					system.setRecordStatus("QUERIED");
				}
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

	@Transactional
	public void checkToCommit(String pActionType, RunwayTriggerRequestDto reqDto) throws Exception {
		log.info("checkToCommit Executing");
		OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		// String query = "";
		// Record rec = null;
		try {
			Integer vButton = 0;
			Integer totalRows = 0;
			// String vTemp = null;
			String vButtonText = null;
			// Object msgnum = messacode;

			if (Objects.equals(system.getFormStatus(), "CHANGED")) {
				if (Objects.equals(pActionType, "COMMIT")) {
					vButtonText = "Cancel";

				}

				else if (Objects.equals(pActionType, "EXIT")) {
					vButtonText = "Exit Without Save";

				}

				else {
					vButtonText = "Cancel Modification";

				}
				if (Objects.equals(global.getLibRefreshed(), "Y")) {
					OracleHelpers.bulkClassMapper(this, displayAlert);
					alertDetails.getCurrent();
					if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						vButton = displayAlert.moreButtons("S", "Refresh Record",
								"You have modified record(s). Select an option:" + chr(10) + chr(10)
										+ "1. Save and refresh Master Library" + chr(10)
										+ "2. Cancel modification, NO Save, NO Refresh",
								"Save&Refresh", vButtonText, "");
						OracleHelpers.bulkClassMapper(displayAlert, this);
						alertDetails.createNewRecord("alertid");
						throw new AlertException(event, alertDetails);
					} else {
						vButton = alertDetails.getAlertValue("alertid", alertDetails.getCurrentAlert());
					}
					if (Arrays.asList(1).contains(vButton)) {

						validateCommit(reqDto);
						try {
							commitForm(this);
							sendUpdatedRowIdDetails();
							coreptLib.message("Record has been saved successfully");
							system.setFormStatus("NORMAL");
							totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
							if (totalRows > 0) {

								OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
								refreshMasterLibrary.refreshRecords(totalRows);

							}

						} catch (Exception e) {
							if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
									|| e instanceof EntityExistsException
									|| e.getCause() instanceof ConstraintViolationException
									|| e.getMessage().contains("ORA-00001")) {
								global.setErrorCode(501);

								coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
										+ "error message from the \"Display \n Error\" in the \"HELP\" menu");

								log.info(" Unique Constrain Error while Executing the keyCommit Service");
							} else {
								throw e;
							}
						}
//						totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
//						if (totalRows > 0) {
//
//							OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
//							refreshMasterLibrary.refreshRecords(totalRows);
//
//						}

						// if ((Objects.equals(msgnum, 40400))) {
						//
						// // TODO CLEAR_MESSAGE;
						// clearMessage();
						//
						// }

					}

				}

				else {
					if (Objects.equals(pActionType, "COMMIT")) {
						vButton = 1;

					}

					else {
						OracleHelpers.bulkClassMapper(this, displayAlert);
						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
							vButton = displayAlert.moreButtons("S", "Refresh Record",
									"Record is modified or inserted or deleted. Select an option: " + "\n ",
									"Save", vButtonText, " ");
							OracleHelpers.bulkClassMapper(displayAlert, this);
							alertDetails.createNewRecord("alertid");
							throw new AlertException(event, alertDetails);
						} else {
							vButton = alertDetails.getAlertValue("alertid", alertDetails.getCurrentAlert());
						}

					}
					if (Objects.equals(vButton, 1)) {

						validateCommit(reqDto);
						try {
							commitForm(this);
							coreptLib.message("Record has been saved successfully");
							system.setFormStatus("NORMAL");
						} catch (Exception e) {
							if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
									|| e instanceof EntityExistsException
									|| e.getCause() instanceof ConstraintViolationException
									|| e.getMessage().contains("ORA-00001")) {
								global.setErrorCode(501);

								coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
										+ "error message from the \"Display \n Error\" in the \"HELP\" menu");

								log.info(" Unique Constrain Error while Executing the keyCommit Service");
							} else {
								throw e;
							}
						}

						// if ((Objects.equals(msgnum, 40400))) {
						//
						// // TODO CLEAR_MESSAGE;
						// clearMessage();
						//
						// }

					}

					else {

						// TODO EXIT_FORM(no_commit);
						exitForm();

					}

				}
				if ((Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 2))
						|| (!Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 1))) {

//					if (Objects.equals(pActionType, "COMMIT")) {
//					}
//
//					else if (Objects.equals(pActionType, "EXIT")) {
//
//						setApplicationProperty("cursorStyle", "default");
//						exitForm();
//
//					}
//
//					else {
//						clearBlock("noCommit", "");
//					}
					if (Objects.equals(pActionType, "COMMIT"))
						;
		        	else if (Objects.equals(pActionType, "EXIT")) {
		            exitForm();
		          }
		        	else {
		        		system.setFormStatus("NORMAL");
		        	}

				}

				else {
					 if (Objects.equals(system.getFormStatus(), "CHANGED")) {
					 throw new FormTriggerFailureException();
					 }else if (Objects.equals(pActionType, "EXIT")) {
							setApplicationProperty("cursorStyle", "default");
							exitForm();
					}

				}

			}

			else {

				OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
				refreshMasterLibrary.ifRefresh();
				if(pActionType.equals("EXIT")) {
					exitForm();
				}
			}

			log.info("checkToCommit Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing checkToCommit" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyEdit(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyEdit Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptTemplate);
			coreptTemplate.keyEdit();
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyDuprec(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDuprec Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			}

			else {
				if (!Objects.equals(nameIn(this, system.getCursorBlock() + ".data_supplier"), null)) {
					createRecord(system.getCurrentBlock());

				}

				duplicateRecord(system.getCurrentBlock(), system.getCursorRecordIndex() + 1);
				copy(this, null, system.getCursorBlock() + ".validate_ind");
				copy(this, null, system.getCursorBlock() + ".create_dcr_number");
				copy(this, null, system.getCursorBlock() + ".update_dcr_number");
				copy(this, null, system.getCursorBlock() + ".processing_cycle");
				if (nameIn(this, system.getCursorBlock() + ".cycle_Data") != null) {
					// null;
				}

				else {
					copy(this, null, system.getCursorBlock() + ".cycle_Data");

				}
				if (Objects.equals(parameter.getRecordType(), "T")) {
					copy(this, "Y", system.getCursorBlock() + ".generated_in_house_flag");
				}

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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyNxtblk(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyNxtblk Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptLib);
			coreptLib.activateRole();
			// null;
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyNxtblk executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyNxtblk Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyPrvblk(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyPrvblk Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// null;
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyPrvblk executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyPrvblk Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyDupItem(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDupItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			} else {

				// TODO duplicate_item;
				duplicateItem(system.getCurrentItem());
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyCrerec(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCrerec Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			} else {
				createRecord(system.getCurrentBlock());

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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyClrfrm(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyClrfrm Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// null
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyClrfrm Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenNewBlockInstance(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenNewBlockInstance Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")
					&& like("PL_%", system.getCursorBlock())) {

				OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
				refreshMasterLibrary.setKeyUpdateFalse(HoneyWellUtils.toCamelCase(system.getCursorBlock()));

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenValidateRecord(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			String pBlock = system.getCursorBlock();

			if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR")) {
				plStdRunwayMrProcessingCycleWhenValidateItem(reqDto);
			} else if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR")) {

				plTldRunwayMrCustomerIdentWhenValidateItem(reqDto);
				plTldRunwayMrProcessingCycleWhenValidateItem(reqDto);
				plTldRunwayMrGeneratedInHouseFlagWhenValidateItem(reqDto);

			}

			if (like("PL_%", pBlock) && Arrays.asList("CHANGED", "INSERT").contains(system.getRecordStatus())) {
				if (!Arrays.asList("Y", "S", "H", "W", "N", "I", "O").contains(nvl(toChar(
						nameIn(this, pBlock + ".validate_Ind")),
						"Y"))) {

					coreptLib.dspMsg("Validate indicator can only 'Y','S','H','W','N' or 'I'");
					throw new FormTriggerFailureException(event);

				}

				if (Objects.equals(parameter.getRecordType(), "T")) {
					coreptLib.validateextrafields(
							toInteger(nameIn(this, pBlock + ".processing_Cycle")),
							!Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "")
									? toChar(nameIn(this, pBlock + ".generated_In_House_Flag"))
									: null);
				}

				if (!Objects.equals(
						nameIn(this, pBlock + ".processing_Cycle"),
						null)
						&& Objects.equals(nameIn(this, pBlock + ".data_Supplier"), null)) {
					copy(this, global.getDataSupplier(), pBlock + ".data_Supplier");
				}

				// TODO do_validate(p_block) --- Program Unit Calling
				doValidate(pBlock, "Y");
				if (!Objects.equals(parameter.getWorkType(), "VIEW")
						&& Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "N")) {
						setItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						if (!like("%MR", pBlock)) {
							setItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "notUpdatable");
							setItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute",
									"notUpdatable");
							setItemProperty(pBlock + ".validate_ind", "current_record_attribute", "default");

						}
					} else {
						setItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						if (!like("%MR", pBlock)) {
							setItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "default");
							setItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute", "default");
							setItemProperty(pBlock + ".validate_ind", "current_record_attribute", "notUpdatable");
						}
					}
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenValidateRecord Service");
			if(Objects.equals(reqDto, null)) {
				throw e;
			}
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyExit(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExit Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
//			OracleHelpers.bulkClassMapper(this, coreptTemplate);
//			coreptTemplate.keyExit();
			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {

				checkToCommit("EXIT",reqDto);

			} else {

				setApplicationProperty("cursorStyle", "default");
				exitForm();
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

	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// displayItemBlockFormPartNumberWhenNewItemInstance(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" displayItemBlockFormPartNumberWhenNewItemInstance Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" displayItemBlockFormPartNumberWhenNewItemInstance executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// displayItemBlockFormPartNumberWhenNewItemInstance Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> displayItemBlockRefreshButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" displayItemBlockRefreshButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer totalRows = null;
			if (Objects.equals(system.getFormStatus(), "CHANGED")) {
				coreptLib.dspMsg("There is changes in the form, please do commit first.");
			} else {
				Integer vButton = null;

				totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
				alertDetails.getCurrent();
				if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
					vButton = moreButtons("S", "Refresh Record",
							"You have modified or inserted or deleted " + totalRows + " record(s). " + chr(10)
									+ "Do you want to refresh the Master Library now?" + chr(10) + chr(10),
							"Refresh", "Cancel", "");
					alertDetails.createNewRecord("refRec");
					throw new AlertException(event, alertDetails);
				} else {
					vButton = alertDetails.getAlertValue("refRec", alertDetails.getCurrentAlert());
				}
				if (Objects.equals(vButton, 1)) {
					refreshMasterLibrary.refreshRecords(totalRows);
				} else {
					// null;
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void validateCommit(RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" validateCommit Executing");
		try {
			Object obj = OracleHelpers.findBlock(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()));
			int i = 0, backupCursorRecordIndex = system.getCursorRecordIndex();
			if (obj instanceof Block<?>) {
				Block<?> blocks = (Block<?>) obj;
				for (Object block : blocks.getData()) {

					// system.setCursorRecordIndex(i);
					if (Objects.equals(nameIn(block, "record_Status"), "INSERT")) {
						system.setRecordStatus("INSERT"); // JJ
						system.setCursorRecordIndex(i); // JJ
						whenValidateRecord(reqDto); // JJ

						preInsert(reqDto);
					} else if (Objects.equals(nameIn(block, "record_Status"), "CHANGED")) {
						system.setRecordStatus("CHANGED"); // J
						system.setCursorRecordIndex(i); // J
						whenValidateRecord(reqDto); // J
						preUpdate(reqDto);
					}
					i = i + 1;
				}
			}
			system.setCursorRecordIndex(backupCursorRecordIndex);
			whenValidateRecord(reqDto);
			
			log.info(" validateCommit executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the validateCommit Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> toolsDuplicate(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> toolsExportDestination(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
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
			if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("stdRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("stdRunwayMr");
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
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					StdRunwayMr stdRunwayMr = app.mapResultSetToClass(mstRec, StdRunwayMr.class);
					reportfile.append(getExportData(stdRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
				}
			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("tldRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("tldRunwayMr");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					TldRunwayMr tldRunwayMr = app.mapResultSetToClass(mstRec, TldRunwayMr.class);
					reportfile.append(getExportData(tldRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
				}

			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("plStdRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("plStdRunwayMr");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					PlStdRunwayMr PlStdRunwayMr = app.mapResultSetToClass(mstRec, PlStdRunwayMr.class);
					reportfile.append(getExportData(PlStdRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
				}
			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("plTldRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("plTldRunwayMr");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					PlTldRunwayMr PlTldRunwayMr = app.mapResultSetToClass(mstRec, PlTldRunwayMr.class);
					reportfile.append(getExportData(PlTldRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
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

	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilDummyWhenButtonPressed(RunwayTriggerRequestDto reqDto) throws
	// Exception{
	// log.info(" webutilDummyWhenButtonPressed Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilDummyWhenButtonPressed executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the webutilDummyWhenButtonPressed Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilClientinfoFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilClientinfoFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilFileFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilFileFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilHostFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilHostFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilSessionFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilSessionFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilFiletransferFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent
	// Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilFiletransferFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilOleFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto reqDto)
	// throws Exception{
	// log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilOleFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilCApiFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilCApiFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilBrowserFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilBrowserFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// public void updateAppInstance() {
	// app.getDb();
	// }
}



-----------------
package com.honeywell.coreptdu.datatypes.runway.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.runway.dto.request.PlTldRunwayMrQuerySearchDto;
import com.honeywell.coreptdu.datatypes.runway.dto.request.PlTldRunwayMrRequestDto;
import com.honeywell.coreptdu.datatypes.runway.entity.PlTldRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.idclass.PlTldRunwayMrIdClass;
import com.honeywell.coreptdu.datatypes.runway.repository.RunwayIPlTldRunwayMrRepository;
import com.honeywell.coreptdu.datatypes.runway.service.IPlTldRunwayMrService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;


import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlTldRunwayMr Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class RunwayPlTldRunwayMrServiceImpl implements IPlTldRunwayMrService 
{

	@Autowired
	RunwayIPlTldRunwayMrRepository pltldrunwaymrRepository;

	@Autowired
	private IApplication app;
	@Autowired
	private HashUtils hashUtils;

	/**
	* Retrieves a list of PlTldRunwayMr with optional pagination.
	*
	* @param page The page number for pagination (optional).
	* @param rec  The number of records per page for pagination (optional).
	* @return A ResponseDto containing the list of PlTldRunwayMr based on the specified page and rec parameters.
	*/
	@Override
	public ResponseEntity<ResponseDto<List<PlTldRunwayMr>>> getAllPlTldRunwayMr(int page, int rec) {
		BaseResponse<List<PlTldRunwayMr>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all PlTldRunwayMr Data");
			if(page == -1 && rec == -1){
			List<PlTldRunwayMr> pltldrunwaymr = pltldrunwaymrRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,pltldrunwaymr));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<PlTldRunwayMr> pltldrunwaymrPages = pltldrunwaymrRepository.findAll(pages);
			if(pltldrunwaymrPages.getContent().size() > 0){
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,pltldrunwaymrPages.getContent(),pltldrunwaymrPages.getTotalElements()));
			} else{
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE,List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all PlTldRunwayMr data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	* Retrieves a specific PlTldRunwayMr data by its ID.
	*
	* @param id The ID of the PlTldRunwayMr to retrieve.
	* @return A ResponseDto containing the PlTldRunwayMr entity with the specified ID.
	*/
	@Override
	public ResponseEntity<ResponseDto<PlTldRunwayMr>> getPlTldRunwayMrById(Long id) {
		BaseResponse<PlTldRunwayMr> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching PlTldRunwayMr Data By Id");
			Optional<PlTldRunwayMr> pltldrunwaymr = pltldrunwaymrRepository.findById(id);
			if (pltldrunwaymr.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, pltldrunwaymr.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching PlTldRunwayMr data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	* Creates new PlTldRunwayMrs based on the provided list of DTOs.
	*
	* @param createpltldrunwaymrs The list of DTOs containing data for creating PlTldRunwayMr.
	* @return A ResponseDto containing the list of created PlTldRunwayMr entities.
	*/
	@Override
	public ResponseEntity<ResponseDto<List<PlTldRunwayMr>>> createPlTldRunwayMr(List<PlTldRunwayMrRequestDto> pltldrunwaymrsCreate) {
		BaseResponse<List<PlTldRunwayMr>> responseObj = new BaseResponse<>();
		List<PlTldRunwayMr> createdPlTldRunwayMrs = new ArrayList<>();

		for (PlTldRunwayMrRequestDto pltldrunwaymrCreate : pltldrunwaymrsCreate) {
			try {
				log.info("Creating PlTldRunwayMr Data");
				PlTldRunwayMr pltldrunwaymr = new PlTldRunwayMr();
				pltldrunwaymr.setDataSupplier(pltldrunwaymrCreate.getDataSupplier());
				pltldrunwaymr.setProcessingCycle(pltldrunwaymrCreate.getProcessingCycle());
				pltldrunwaymr.setFileRecno((pltldrunwaymrCreate.getFileRecno()));
				pltldrunwaymr.setCycleData(pltldrunwaymrCreate.getCycleData());
				pltldrunwaymr.setValidateInd(pltldrunwaymrCreate.getValidateInd());
				pltldrunwaymr.setCreateDcrNumber(pltldrunwaymrCreate.getCreateDcrNumber());
				pltldrunwaymr.setUpdateDcrNumber(pltldrunwaymrCreate.getUpdateDcrNumber());
				pltldrunwaymr.setRunwayLatitude(pltldrunwaymrCreate.getRunwayLatitude());
				pltldrunwaymr.setRunwayLongitude(pltldrunwaymrCreate.getRunwayLongitude());
				pltldrunwaymr.setDataSupplier(pltldrunwaymrCreate.getDataSupplier());
				pltldrunwaymr.setRunwayAccuracyCompInd(pltldrunwaymrCreate.getRunwayAccuracyCompInd());
				pltldrunwaymr.setCustomerIdent(pltldrunwaymrCreate.getCustomerIdent());
				pltldrunwaymr.setSecondLocalizerClass(pltldrunwaymrCreate.getSecondLocalizerClass());
				pltldrunwaymr.setAirportIdent(pltldrunwaymrCreate.getAirportIdent());
				pltldrunwaymr.setRunwayLength(pltldrunwaymrCreate.getRunwayLength());
				pltldrunwaymr.setRunwayWidth(pltldrunwaymrCreate.getRunwayWidth());
				pltldrunwaymr.setDisplacedThresholdDistance(pltldrunwaymrCreate.getDisplacedThresholdDistance());
				pltldrunwaymr.setRunwayDescription(pltldrunwaymrCreate.getRunwayDescription());
				pltldrunwaymr.setSecondLocalizerMlsGlsIdent(pltldrunwaymrCreate.getSecondLocalizerMlsGlsIdent());
				pltldrunwaymr.setLandThreselevAccrCompInd(pltldrunwaymrCreate.getLandThreselevAccrCompInd());
				pltldrunwaymr.setRunwayIdent(pltldrunwaymrCreate.getRunwayIdent());
				pltldrunwaymr.setLocalizerMlsClass(pltldrunwaymrCreate.getLocalizerMlsClass());
				pltldrunwaymr.setThresholdCrossingHeight(pltldrunwaymrCreate.getThresholdCrossingHeight());
				pltldrunwaymr.setCycleData(pltldrunwaymrCreate.getCycleData());
				pltldrunwaymr.setLocalizerMlsGlsIdent(pltldrunwaymrCreate.getLocalizerMlsGlsIdent());
				pltldrunwaymr.setFileRecno(pltldrunwaymrCreate.getFileRecno());
				pltldrunwaymr.setLandingThresholdElevation(pltldrunwaymrCreate.getLandingThresholdElevation());
				pltldrunwaymr.setValidateInd(pltldrunwaymrCreate.getValidateInd());
				pltldrunwaymr.setUpdateDcrNumber(pltldrunwaymrCreate.getUpdateDcrNumber());
				pltldrunwaymr.setAirportIcao(pltldrunwaymrCreate.getAirportIcao());
				pltldrunwaymr.setStopway(pltldrunwaymrCreate.getStopway());
				pltldrunwaymr.setRunwayGradient(pltldrunwaymrCreate.getRunwayGradient());
				pltldrunwaymr.setRunwayMagneticBearing(pltldrunwaymrCreate.getRunwayMagneticBearing());
				pltldrunwaymr.setGeneratedInHouseFlag(pltldrunwaymrCreate.getGeneratedInHouseFlag());
				pltldrunwaymr.setProcessingCycle(pltldrunwaymrCreate.getProcessingCycle());
				pltldrunwaymr.setCreateDcrNumber(pltldrunwaymrCreate.getCreateDcrNumber());
				PlTldRunwayMr createdPlTldRunwayMr = pltldrunwaymrRepository.save(pltldrunwaymr);
				createdPlTldRunwayMrs.add(createdPlTldRunwayMr);
			} catch (Exception ex) {
				log.error("An error occurred while creating PlTldRunwayMr data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdPlTldRunwayMrs));
	}

	/**
	* Updates existing PlTldRunwayMrs based on the provided list of DTOs.
	*
	* @param pltldrunwaymrsUpdate The list of DTOs containing data for updating PlTldRunwayMr.
	* @return A ResponseDto containing the list of updated PlTldRunwayMr entities.
	*/
	@Override
	public ResponseEntity<ResponseDto<List<PlTldRunwayMr>>> updatePlTldRunwayMr(List<PlTldRunwayMrRequestDto> pltldrunwaymrsUpdate) {
		BaseResponse<List<PlTldRunwayMr>> responseObj = new BaseResponse<>();
		List<PlTldRunwayMr> updatedPlTldRunwayMrs = new ArrayList<>();

		for (PlTldRunwayMrRequestDto pltldrunwaymrUpdate : pltldrunwaymrsUpdate) {
			try {
				log.info("Updating PlTldRunwayMr Data");
				PlTldRunwayMrIdClass PlTldRunwayMrId = new PlTldRunwayMrIdClass(pltldrunwaymrUpdate.getAirportIcao(),pltldrunwaymrUpdate.getAirportIdent(),pltldrunwaymrUpdate.getDataSupplier(),pltldrunwaymrUpdate.getProcessingCycle(),pltldrunwaymrUpdate.getCustomerIdent(),pltldrunwaymrUpdate.getRunwayIdent());
				Optional<PlTldRunwayMr> existingPlTldRunwayMrOptional = pltldrunwaymrRepository.findById(PlTldRunwayMrId);
				if (existingPlTldRunwayMrOptional.isPresent()) {
					PlTldRunwayMr existingPlTldRunwayMr = existingPlTldRunwayMrOptional.get();
						existingPlTldRunwayMr.setDataSupplier(pltldrunwaymrUpdate.getDataSupplier());
						existingPlTldRunwayMr.setProcessingCycle(pltldrunwaymrUpdate.getProcessingCycle());
						existingPlTldRunwayMr.setFileRecno(pltldrunwaymrUpdate.getFileRecno());
						existingPlTldRunwayMr.setCycleData(pltldrunwaymrUpdate.getCycleData());
						existingPlTldRunwayMr.setValidateInd(pltldrunwaymrUpdate.getValidateInd());
						existingPlTldRunwayMr.setCreateDcrNumber(pltldrunwaymrUpdate.getCreateDcrNumber());
						existingPlTldRunwayMr.setUpdateDcrNumber(pltldrunwaymrUpdate.getUpdateDcrNumber());
						existingPlTldRunwayMr.setRunwayLatitude(pltldrunwaymrUpdate.getRunwayLatitude());
						existingPlTldRunwayMr.setRunwayLongitude(pltldrunwaymrUpdate.getRunwayLongitude());
						existingPlTldRunwayMr.setDataSupplier(pltldrunwaymrUpdate.getDataSupplier());
						existingPlTldRunwayMr.setRunwayAccuracyCompInd(pltldrunwaymrUpdate.getRunwayAccuracyCompInd());
						existingPlTldRunwayMr.setCustomerIdent(pltldrunwaymrUpdate.getCustomerIdent());
						existingPlTldRunwayMr.setSecondLocalizerClass(pltldrunwaymrUpdate.getSecondLocalizerClass());
						existingPlTldRunwayMr.setAirportIdent(pltldrunwaymrUpdate.getAirportIdent());
						existingPlTldRunwayMr.setRunwayLength(pltldrunwaymrUpdate.getRunwayLength());
						existingPlTldRunwayMr.setRunwayWidth(pltldrunwaymrUpdate.getRunwayWidth());
						existingPlTldRunwayMr.setDisplacedThresholdDistance(pltldrunwaymrUpdate.getDisplacedThresholdDistance());
						existingPlTldRunwayMr.setRunwayDescription(pltldrunwaymrUpdate.getRunwayDescription());
						existingPlTldRunwayMr.setSecondLocalizerMlsGlsIdent(pltldrunwaymrUpdate.getSecondLocalizerMlsGlsIdent());
						existingPlTldRunwayMr.setLandThreselevAccrCompInd(pltldrunwaymrUpdate.getLandThreselevAccrCompInd());
						existingPlTldRunwayMr.setRunwayIdent(pltldrunwaymrUpdate.getRunwayIdent());
						existingPlTldRunwayMr.setLocalizerMlsClass(pltldrunwaymrUpdate.getLocalizerMlsClass());
						existingPlTldRunwayMr.setThresholdCrossingHeight(pltldrunwaymrUpdate.getThresholdCrossingHeight());
						existingPlTldRunwayMr.setCycleData(pltldrunwaymrUpdate.getCycleData());
						existingPlTldRunwayMr.setLocalizerMlsGlsIdent(pltldrunwaymrUpdate.getLocalizerMlsGlsIdent());
						existingPlTldRunwayMr.setFileRecno(pltldrunwaymrUpdate.getFileRecno());
						existingPlTldRunwayMr.setLandingThresholdElevation(pltldrunwaymrUpdate.getLandingThresholdElevation());
						existingPlTldRunwayMr.setValidateInd(pltldrunwaymrUpdate.getValidateInd());
						existingPlTldRunwayMr.setUpdateDcrNumber(pltldrunwaymrUpdate.getUpdateDcrNumber());
						existingPlTldRunwayMr.setAirportIcao(pltldrunwaymrUpdate.getAirportIcao());
						existingPlTldRunwayMr.setStopway(pltldrunwaymrUpdate.getStopway());
						existingPlTldRunwayMr.setRunwayGradient(pltldrunwaymrUpdate.getRunwayGradient());
						existingPlTldRunwayMr.setRunwayMagneticBearing(pltldrunwaymrUpdate.getRunwayMagneticBearing());
						existingPlTldRunwayMr.setGeneratedInHouseFlag(pltldrunwaymrUpdate.getGeneratedInHouseFlag());
						existingPlTldRunwayMr.setProcessingCycle(pltldrunwaymrUpdate.getProcessingCycle());
						existingPlTldRunwayMr.setCreateDcrNumber(pltldrunwaymrUpdate.getCreateDcrNumber());
					PlTldRunwayMr updatedPlTldRunwayMr = pltldrunwaymrRepository.save(existingPlTldRunwayMr);
					updatedPlTldRunwayMrs.add(updatedPlTldRunwayMr);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while updating PlTldRunwayMr data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedPlTldRunwayMrs));
	}

	/**
	* Deletes existing PlTldRunwayMrs based on the provided list of DTOs.
	*
	* @param deletepltldrunwaymrs The list of DTOs containing data for deleting PlTldRunwayMr.
	* @return A ResponseDto containing the list of deleted PlTldRunwayMr entities.
	*/
	@Override
	public ResponseEntity<ResponseDto<List<PlTldRunwayMr>>> deletePlTldRunwayMr(List<PlTldRunwayMrRequestDto> pltldrunwaymrDeletes) {
		BaseResponse<List<PlTldRunwayMr>> responseObj = new BaseResponse<>();
		List<PlTldRunwayMr> deletedPlTldRunwayMrs = new ArrayList<>();

		for (PlTldRunwayMrRequestDto pltldrunwaymrDelete : pltldrunwaymrDeletes) {
			try {
				log.info("Deleting PlTldRunwayMr Data");
				PlTldRunwayMrIdClass PlTldRunwayMrId = new PlTldRunwayMrIdClass(pltldrunwaymrDelete.getAirportIcao(),pltldrunwaymrDelete.getAirportIdent(),pltldrunwaymrDelete.getDataSupplier(),pltldrunwaymrDelete.getProcessingCycle(),pltldrunwaymrDelete.getCustomerIdent(),pltldrunwaymrDelete.getRunwayIdent());
				Optional<PlTldRunwayMr> existingPlTldRunwayMrOptional = pltldrunwaymrRepository.findById(PlTldRunwayMrId);
				if (existingPlTldRunwayMrOptional.isPresent()) {
					PlTldRunwayMr existingPlTldRunwayMr = existingPlTldRunwayMrOptional.get();
					pltldrunwaymrRepository.deleteById(PlTldRunwayMrId);
					deletedPlTldRunwayMrs.add(existingPlTldRunwayMr);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while deleting PlTldRunwayMr data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedPlTldRunwayMrs));
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldRunwayMr>>> searchPlTldRunwayMr(PlTldRunwayMrQuerySearchDto pltldrunwaymrQuerySearch,int page,int rec) {
		BaseResponse<List<PlTldRunwayMr>> responseObj = new BaseResponse<>();
		List<PlTldRunwayMr> searchPlTldRunwayMrs = new ArrayList<>();

		try{
			Long total = 0L;
			// Total Count Process
			String where = hashUtils.decrypt(pltldrunwaymrQuerySearch.getCallFormWhere());
			String countQuery =app.getQuery(pltldrunwaymrQuerySearch,"pl_tld_runway",where,"customer_ident,airport_icao, airport_ident, runway_ident,processing_cycle",true,page==-1||rec==-1?true:false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			 String searchQuery = app.getQuery(pltldrunwaymrQuerySearch,"pl_tld_runway",where,"customer_ident,airport_icao, airport_ident, runway_ident,processing_cycle",false,false);
			 List<Record> records = null;
			 if(page==-1||rec==-1){
				 records = app.executeQuery(searchQuery,0,100);
			 }else{
				 records = app.executeQuery(searchQuery,page,rec);
			 }
			 int i=0;
				for (Record searchRec : records) {
						searchPlTldRunwayMrs.add(app.mapResultSetToClass(searchRec, PlTldRunwayMr.class));
						searchPlTldRunwayMrs.get(i).setOldProcessingCycle(OracleHelpers.toInteger(searchPlTldRunwayMrs.get(i).getProcessingCycle()));
						i++;
				}
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchPlTldRunwayMrs, total));
		}
		catch(Exception e){
			return responseObj.render(responseObj.formErrorResponse(200, Constants.RECORD_NOT_FOUND_MESSAGE));
}
	}
}
----------------------
package com.honeywell.coreptdu.datatypes.runway.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.runway.dto.request.StdRunwayMrQuerySearchDto;
import com.honeywell.coreptdu.datatypes.runway.entity.StdRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.service.IStdRunwayMrService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * StdRunwayMr Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class RunwayStdRunwayMrServiceImpl implements IStdRunwayMrService {

	// @Autowired
	// IStdRunwayMrRepository stdrunwaymrRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of StdRunwayMr with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of StdRunwayMr based on the
	 *         specified page and rec parameters.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<StdRunwayMr>>> getAllStdRunwayMr(int
	// page, int rec) {
	// BaseResponse<List<StdRunwayMr>> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching all StdRunwayMr Data");
	// if (page == -1 && rec == -1) {
	// List<StdRunwayMr> stdrunwaymr = stdrunwaymrRepository.findAll();
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// stdrunwaymr));
	// }
	// Pageable pages = PageRequest.of(page, rec);
	// Page<StdRunwayMr> stdrunwaymrPages = stdrunwaymrRepository.findAll(pages);
	// if (stdrunwaymrPages.getContent().size() > 0) {
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// stdrunwaymrPages.getContent(), stdrunwaymrPages.getTotalElements()));
	// } else {
	// return responseObj
	// .render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE,
	// List.of()));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching all StdRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Retrieves a specific StdRunwayMr data by its ID.
	// *
	// * @param id The ID of the StdRunwayMr to retrieve.
	// * @return A ResponseDto containing the StdRunwayMr entity with the specified
	// * ID.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<StdRunwayMr>> getStdRunwayMrById(Long id) {
	// BaseResponse<StdRunwayMr> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching StdRunwayMr Data By Id");
	// Optional<StdRunwayMr> stdrunwaymr = stdrunwaymrRepository.findById(id);
	// if (stdrunwaymr.isPresent()) {
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// stdrunwaymr.get()));
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching StdRunwayMr data by Id",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }

	/**
	 * Creates new StdRunwayMrs based on the provided list of DTOs.
	 *
	 * @param createstdrunwaymrs The list of DTOs containing data for creating
	 *                           StdRunwayMr.
	 * @return A ResponseDto containing the list of created StdRunwayMr entities.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<StdRunwayMr>>> createStdRunwayMr(
	// List<StdRunwayMrRequestDto> stdrunwaymrsCreate) {
	// BaseResponse<List<StdRunwayMr>> responseObj = new BaseResponse<>();
	// List<StdRunwayMr> createdStdRunwayMrs = new ArrayList<>();
	//
	// for (StdRunwayMrRequestDto stdrunwaymrCreate : stdrunwaymrsCreate) {
	// try {
	// log.info("Creating StdRunwayMr Data");
	// StdRunwayMr stdrunwaymr = new StdRunwayMr();
	// StdRunwayMr createdStdRunwayMr = stdrunwaymrRepository.save(stdrunwaymr);
	// createdStdRunwayMrs.add(createdStdRunwayMr);
	// } catch (Exception ex) {
	// log.error("An error occurred while creating StdRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED,
	// createdStdRunwayMrs));
	// }
	//
	// /**
	// * Updates existing StdRunwayMrs based on the provided list of DTOs.
	// *
	// * @param stdrunwaymrsUpdate The list of DTOs containing data for updating
	// * StdRunwayMr.
	// * @return A ResponseDto containing the list of updated StdRunwayMr entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<StdRunwayMr>>> updateStdRunwayMr(
	// List<StdRunwayMrRequestDto> stdrunwaymrsUpdate) {
	// BaseResponse<List<StdRunwayMr>> responseObj = new BaseResponse<>();
	// List<StdRunwayMr> updatedStdRunwayMrs = new ArrayList<>();
	//
	// for (StdRunwayMrRequestDto stdrunwaymrUpdate : stdrunwaymrsUpdate) {
	// try {
	// log.info("Updating StdRunwayMr Data");
	// if (existingStdRunwayMrOptional.isPresent()) {
	// StdRunwayMr existingStdRunwayMr = existingStdRunwayMrOptional.get();
	// StdRunwayMr updatedStdRunwayMr =
	// stdrunwaymrRepository.save(existingStdRunwayMr);
	// updatedStdRunwayMrs.add(updatedStdRunwayMr);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while updating StdRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS,
	// updatedStdRunwayMrs));
	// }
	//
	// /**
	// * Deletes existing StdRunwayMrs based on the provided list of DTOs.
	// *
	// * @param deletestdrunwaymrs The list of DTOs containing data for deleting
	// * StdRunwayMr.
	// * @return A ResponseDto containing the list of deleted StdRunwayMr entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<StdRunwayMr>>> deleteStdRunwayMr(
	// List<StdRunwayMrRequestDto> stdrunwaymrDeletes) {
	// BaseResponse<List<StdRunwayMr>> responseObj = new BaseResponse<>();
	// List<StdRunwayMr> deletedStdRunwayMrs = new ArrayList<>();
	//
	// for (StdRunwayMrRequestDto stdrunwaymrDelete : stdrunwaymrDeletes) {
	// try {
	// log.info("Deleting StdRunwayMr Data");
	// Optional<StdRunwayMr> existingStdRunwayMrOptional =
	// stdrunwaymrRepository.findById(StdRunwayMrId);
	// if (existingStdRunwayMrOptional.isPresent()) {
	// StdRunwayMr existingStdRunwayMr = existingStdRunwayMrOptional.get();
	// stdrunwaymrRepository.deleteById(existingStdRunwayMr.getId());
	// deletedStdRunwayMrs.add(existingStdRunwayMr);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while deleting StdRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE,
	// deletedStdRunwayMrs));
	// }
	//
	@Override
	public ResponseEntity<ResponseDto<List<StdRunwayMr>>> searchStdRunwayMr(
			StdRunwayMrQuerySearchDto stdrunwaymrQuerySearch, int page, int rec) {
		BaseResponse<List<StdRunwayMr>> responseObj = new BaseResponse<>();
		List<StdRunwayMr> searchStdRunwayMrs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(stdrunwaymrQuerySearch, "std_runway", "",
					"airport_icao, airport_ident, runway_ident", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(stdrunwaymrQuerySearch, "std_runway", "",
					"airport_icao, airport_ident, runway_ident", false, false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery, 0, 100);
			} else {
				records = app.executeQuery(searchQuery, page, rec);
			}
			for (Record searchRec : records) {
				searchStdRunwayMrs.add(app.mapResultSetToClass(searchRec, StdRunwayMr.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchStdRunwayMrs, total));
		} catch (Exception e) {
			log.error("An error occurred while fetching data", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(200, Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}

-------------------------
package com.honeywell.coreptdu.datatypes.runway.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.runway.dto.request.TldRunwayMrQuerySearchDto;
import com.honeywell.coreptdu.datatypes.runway.entity.TldRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.service.ITldRunwayMrService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * TldRunwayMr Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class RunwayTldRunwayMrServiceImpl implements ITldRunwayMrService {

	// @Autowired
	// ITldRunwayMrRepository tldrunwaymrRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of TldRunwayMr with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of TldRunwayMr based on the
	 *         specified page and rec parameters.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<TldRunwayMr>>> getAllTldRunwayMr(int
	// page, int rec) {
	// BaseResponse<List<TldRunwayMr>> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching all TldRunwayMr Data");
	// if(page == -1 && rec == -1){
	// List<TldRunwayMr> tldrunwaymr = tldrunwaymrRepository.findAll();
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,tldrunwaymr));
	// }
	// Pageable pages = PageRequest.of(page, rec);
	// Page<TldRunwayMr> tldrunwaymrPages = tldrunwaymrRepository.findAll(pages);
	// if(tldrunwaymrPages.getContent().size() > 0){
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,tldrunwaymrPages.getContent(),tldrunwaymrPages.getTotalElements()));
	// } else{
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE,List.of()));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching all TldRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Retrieves a specific TldRunwayMr data by its ID.
	// *
	// * @param id The ID of the TldRunwayMr to retrieve.
	// * @return A ResponseDto containing the TldRunwayMr entity with the specified
	// ID.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<TldRunwayMr>> getTldRunwayMrById(Long id) {
	// BaseResponse<TldRunwayMr> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching TldRunwayMr Data By Id");
	// Optional<TldRunwayMr> tldrunwaymr = tldrunwaymrRepository.findById(id);
	// if (tldrunwaymr.isPresent()) {
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// tldrunwaymr.get()));
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching TldRunwayMr data by Id",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }

	/**
	 * Creates new TldRunwayMrs based on the provided list of DTOs.
	 *
	 * @param createtldrunwaymrs The list of DTOs containing data for creating
	 *                           TldRunwayMr.
	 * @return A ResponseDto containing the list of created TldRunwayMr entities.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<TldRunwayMr>>>
	// createTldRunwayMr(List<TldRunwayMrRequestDto> tldrunwaymrsCreate) {
	// BaseResponse<List<TldRunwayMr>> responseObj = new BaseResponse<>();
	// List<TldRunwayMr> createdTldRunwayMrs = new ArrayList<>();
	//
	// for (TldRunwayMrRequestDto tldrunwaymrCreate : tldrunwaymrsCreate) {
	// try {
	// log.info("Creating TldRunwayMr Data");
	// TldRunwayMr tldrunwaymr = new TldRunwayMr();
	// TldRunwayMr createdTldRunwayMr = tldrunwaymrRepository.save(tldrunwaymr);
	// createdTldRunwayMrs.add(createdTldRunwayMr);
	// } catch (Exception ex) {
	// log.error("An error occurred while creating TldRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED,
	// createdTldRunwayMrs));
	// }
	//
	// /**
	// * Updates existing TldRunwayMrs based on the provided list of DTOs.
	// *
	// * @param tldrunwaymrsUpdate The list of DTOs containing data for updating
	// TldRunwayMr.
	// * @return A ResponseDto containing the list of updated TldRunwayMr entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<TldRunwayMr>>>
	// updateTldRunwayMr(List<TldRunwayMrRequestDto> tldrunwaymrsUpdate) {
	// BaseResponse<List<TldRunwayMr>> responseObj = new BaseResponse<>();
	// List<TldRunwayMr> updatedTldRunwayMrs = new ArrayList<>();
	//
	// for (TldRunwayMrRequestDto tldrunwaymrUpdate : tldrunwaymrsUpdate) {
	// try {
	// log.info("Updating TldRunwayMr Data");
	// if (existingTldRunwayMrOptional.isPresent()) {
	// TldRunwayMr existingTldRunwayMr = existingTldRunwayMrOptional.get();
	// TldRunwayMr updatedTldRunwayMr =
	// tldrunwaymrRepository.save(existingTldRunwayMr);
	// updatedTldRunwayMrs.add(updatedTldRunwayMr);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while updating TldRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS,
	// updatedTldRunwayMrs));
	// }
	//
	// /**
	// * Deletes existing TldRunwayMrs based on the provided list of DTOs.
	// *
	// * @param deletetldrunwaymrs The list of DTOs containing data for deleting
	// TldRunwayMr.
	// * @return A ResponseDto containing the list of deleted TldRunwayMr entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<TldRunwayMr>>>
	// deleteTldRunwayMr(List<TldRunwayMrRequestDto> tldrunwaymrDeletes) {
	// BaseResponse<List<TldRunwayMr>> responseObj = new BaseResponse<>();
	// List<TldRunwayMr> deletedTldRunwayMrs = new ArrayList<>();
	//
	// for (TldRunwayMrRequestDto tldrunwaymrDelete : tldrunwaymrDeletes) {
	// try {
	// log.info("Deleting TldRunwayMr Data");
	// Optional<TldRunwayMr> existingTldRunwayMrOptional =
	// tldrunwaymrRepository.findById(TldRunwayMrId);
	// if (existingTldRunwayMrOptional.isPresent()) {
	// TldRunwayMr existingTldRunwayMr = existingTldRunwayMrOptional.get();
	// tldrunwaymrRepository.deleteById(existingTldRunwayMr.getId());
	// deletedTldRunwayMrs.add(existingTldRunwayMr);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while deleting TldRunwayMr data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE,
	// deletedTldRunwayMrs));
	// }

	@Override
	public ResponseEntity<ResponseDto<List<TldRunwayMr>>> searchTldRunwayMr(
			TldRunwayMrQuerySearchDto tldrunwaymrQuerySearch, int page, int rec) {
		BaseResponse<List<TldRunwayMr>> responseObj = new BaseResponse<>();
		List<TldRunwayMr> searchTldRunwayMrs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(tldrunwaymrQuerySearch, "tld_runway", "",
					"customer_ident,airport_icao, airport_ident, runway_ident", true,
					page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(tldrunwaymrQuerySearch, "tld_runway", "",
					"customer_ident,airport_icao, airport_ident, runway_ident", false, false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery, 0, 100);
			} else {
				records = app.executeQuery(searchQuery, page, rec);
			}
			for (Record searchRec : records) {
				searchTldRunwayMrs.add(app.mapResultSetToClass(searchRec, TldRunwayMr.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchTldRunwayMrs, total));
		} catch (Exception e) {
			log.error("An error occurred while fetching data", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(200, Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}

------------------------------------
package com.honeywell.coreptdu.datatypes.runway.serviceimpl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.NonUniqueObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.coreptmenummb.serviceimpl.CoreptMenuMmbServiceImpl;
import com.honeywell.coreptdu.datatypes.corepttemplate.block.DisplayItemBlock;
import com.honeywell.coreptdu.datatypes.corepttemplate.serviceimpl.CoreptTemplateTriggerServiceImpl;
import com.honeywell.coreptdu.datatypes.exportdestination.block.SelectOptions;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.request.ExportDestinationTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.response.ExportDestinationTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.runway.block.ControlBlock;
import com.honeywell.coreptdu.datatypes.runway.block.Webutil;
import com.honeywell.coreptdu.datatypes.runway.dto.request.RunwayTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.runway.dto.response.RunwayTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.runway.entity.PlStdRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.PlTldRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.StdRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.entity.TldRunwayMr;
import com.honeywell.coreptdu.datatypes.runway.service.IRunwayTriggerService;
import com.honeywell.coreptdu.exception.AlertException;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.exception.FormTriggerFailureException;
import com.honeywell.coreptdu.global.dbtype.CrRunway;
import com.honeywell.coreptdu.global.dbtype.PlStdRunway;
import com.honeywell.coreptdu.global.dbtype.PlTldRunway;
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
import com.honeywell.coreptdu.pkg.body.DisplayAlert;
import com.honeywell.coreptdu.pkg.body.RefreshMasterLibrary;
import com.honeywell.coreptdu.pll.CoreptLib;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.GenericNativeQueryHelper;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInOutParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureOutParameter;
import com.honeywell.coreptdu.utils.oracleutils.Block;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.HoneyWellUtils;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.PropertyHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroup;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;

@Slf4j
@Service
@RequestScope
public class RunwayTriggerServiceImpl extends GenericTemplateForm<RunwayTriggerServiceImpl>
		implements IRunwayTriggerService {

	@Getter
	@Setter
	private Webutil webutil = new Webutil();
	@Getter
	@Setter
	private Block<PlTldRunwayMr> plTldRunwayMr = new Block<>();
	@Getter
	@Setter
	private ControlBlock controlBlock = new ControlBlock();
	@Getter
	@Setter
	private Block<PlStdRunwayMr> plStdRunwayMr = new Block<>();
	@Getter
	@Setter
	private Block<TldRunwayMr> tldRunwayMr = new Block<>();
	@Getter
	@Setter
	private Block<StdRunwayMr> stdRunwayMr = new Block<>();
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
	private List<Event> event = new ArrayList<>();
	@Getter
	@Setter
	private HashMap<String, RecordGroup> groups = new HashMap<>();
	@Getter
	@Setter
	private List<String> blocksOrder = new ArrayList<>();
	@Getter
	@Setter
	private Map<String, WindowDetail> windows = new HashMap<>();

	@Autowired
	@Getter
	@Setter
	private IApplication app;

	@Autowired
	private CoreptLib coreptLib;

	@Autowired
	private CoreptTemplateTriggerServiceImpl coreptTemplate;

	@Autowired
	private RefreshMasterLibrary refreshMasterLibrary;

	@Autowired
	private HashUtils hashUtils;

	@Getter
	@Setter
	private SelectOptions selectOptions = new SelectOptions();

	@Autowired
	private CoreptMenuMmbServiceImpl coreptMenuMmbServiceImpl;

	@Getter
	@Setter
	@Autowired
	private DisplayAlert displayAlert = new DisplayAlert();
	@Getter
	@Setter
	private AlertDetail alertDetails = new AlertDetail();

	@Autowired
	private GenericNativeQueryHelper genericNativeQueryHelper;

	public void updateAppInstance() {

		super.app = this.app;
		super.baseInstance = this;
		super.groups = this.groups;
		super.genericNativeQueryHelper = this.genericNativeQueryHelper;
		super.event = this.event;
		super.parameter = this.parameter;
		super.displayAlert = this.displayAlert;
		this.system.setCursorBlock(toSnakeCase(system.getCursorBlock()));
		super.system = this.system;
		super.global = this.global;
		super.blocksOrder = this.blocksOrder;
		super.windows = this.windows;
		// super.displayItemBlock = this.displayItemBlock;
		OracleHelpers.bulkClassMapper(this, displayAlert);
		OracleHelpers.bulkClassMapper(this, coreptLib);
		coreptLib.initialization(this);
		OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		coreptMenuMmbServiceImpl.initialization(this);
		OracleHelpers.bulkClassMapper(this, coreptTemplate);
		coreptTemplate.initialization(this);
		OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		refreshMasterLibrary.initialization(this);

	}

	// TODO PUnits Manual configuration
	// ParentName ---> CHECK_TO_COMMIT
	// File Name ---> corept_template.fmb
	// TODO PUnits Manual configuration
	// ParentName ---> DSP_ERROR
	// File Name ---> corept_template.fmb

	@Override
	public void doValidate(String pBlock, String pIgnoreRef) throws Exception {
		log.info("doValidate Executing");
		// String query = "";
		// Record rec = null;
		try {
			// Object vRecord = null;
			Integer vErrInd = 0;
			List<Integer> vErrList = new ArrayList<>();
			String vAllErr = null;
			PlStdRunway vSrec = new PlStdRunway();
			PlTldRunway vTrec = new PlTldRunway();
			String vNerr = "YYYYY";
			String vValid = null;
			Integer vCycle = null;
			String vInd = "Y";
			CrRunway vRecord = new CrRunway();

			if (!Objects.equals(toString(
					nameIn(this, pBlock + ".RUNWAY_IDENT")),
					null)) {

				// TODO Set_Application_Property(cursor_style,"BUSY");
				if (Objects.equals(parameter.getRecordType(), "S")) {
					controlBlock.setStdOverrideErrors(null);

				}

				else {
					controlBlock.setTldOverrideErrors(null);

				}

				// TODO populate_record(p_block,v_record,v_cycle) --- Program Unit Calling
				Map<String, Object> res = populateRecord(pBlock);
				vRecord = (CrRunway) res.get("pRecord");
				vCycle = (Integer) res.get("pCycle");

				// TODO
				// RECSV1.VRUNWAY(global.getDataSupplier(),v_cycle,v_record,v_err_list,v_err_ind);
				String vcyc = toString(vCycle);
				Map<String, Object> dbCall = app.executeProcedure("CPT", "VRUNWAY", "RECSV1",
						new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_proc_cycle", vcyc, OracleTypes.VARCHAR),
						new ProcedureInOutParameter("p_crecord", vRecord, OracleTypes.STRUCT, "SDV_RECORDS.CR_RUNWAY"),
						new ProcedureOutParameter("p_errlist", OracleTypes.ARRAY, "CPT_TYPES.ERRLIST_TYPE"),
						new ProcedureOutParameter("v_err_ind", OracleTypes.NUMBER));

				Struct outStruct = (Struct) dbCall.get("p_crecord");
				// Object[] updatedRecord = outStruct.getAttributes();
				outStruct.getAttributes();
				Array errList = (Array) dbCall.get("p_errlist");
				BigDecimal[] bg = (BigDecimal[]) errList.getArray();
				vErrInd = toInteger(dbCall.get("v_err_ind"));

				for (BigDecimal val : bg) {
					vErrList.add(val.intValue());
				}

				if (!Objects.equals(vErrInd, 0)) {
					for (int i = 0; i <= vErrList.size() - 1; i++) {
						if (!coreptLib.isOverride(global.getDataSupplier(), vCycle, "RUNWAY",
								vErrList.get(i))) {
							vAllErr = getNullClean(vAllErr) + " * " + toChar(vErrList.get(i)) + " - "
									+ coreptLib.getErrText(vErrList.get(i));
							vInd = "I";
						}

						else {
							if (!Objects.equals(vInd, "I")) {
								vInd = "O";

							}

						}

					}

				}

				// TODO populate_rel_record(p_block,v_srec,v_trec) --- Program Unit Calling
				Map<String, Object> res1 = populateRelRecord(pBlock);
				// PlTldRunway
				vSrec = Optional.ofNullable((PlStdRunway) res1.get("pSrec")).orElseGet(PlStdRunway::new);

				//vSrec = (PlStdRunway) res1.get("pSrec");
				vTrec = (PlTldRunway) res1.get("pTrec");

				// TODO
				// recrv1.vrunway(parameter.getRecordType(),v_srec,v_trec,v_nerr,v_valid,p_ignore_ref,"DU");

				Map<String, Object> dbCall1 = app.executeProcedure("CPTS", "vrunway_wrapper", "RECRV1_WRAPPER",
						new ProcedureInParameter("p_record_type", parameter.getRecordType(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_srec", vSrec, OracleTypes.STRUCT,
								"RECRV1_WRAPPER.PL_STD_RUNWAY_TYPE"),
						new ProcedureInParameter("p_trec", vTrec, OracleTypes.STRUCT,
								"RECRV1_WRAPPER.PL_TLD_RUNWAY_TYPE"),
						new ProcedureOutParameter("p_err", OracleTypes.VARCHAR),
						new ProcedureOutParameter("p_valind", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_view_only", pIgnoreRef, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_run_loc", "DU", OracleTypes.VARCHAR));

				vNerr = (String) dbCall1.get("p_err");
				vValid = (String) dbCall1.get("p_valind");

				// coverity-fixes
				log.info(vValid);
				// dbCall1.get("p_valind");

				if (!Objects.equals(vNerr, "YYYYY")) {
					if (Objects.equals(substr(vNerr, 1, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 216 - " + coreptLib.getErrText(216);
					}

					if (Objects.equals(substr(vNerr, 2, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 266 - " + coreptLib.getErrText(266);

					}

					if (Objects.equals(substr(vNerr, 3, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 648 - " + coreptLib.getErrText(648);

					}

					if (Objects.equals(substr(vNerr, 4, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 655 - " + coreptLib.getErrText(655);

					}

					if (Objects.equals(substr(vNerr, 5, 1), "N")) {
						vAllErr = getNullClean(vAllErr) + " * 1167 - " + coreptLib.getErrText(1167);

					}

				}

				// TODO
				// Set_Ind_and_Message(p_block,v_all_err,parameter.getWorkType(),p_ignore_ref,v_ind);
				coreptLib.setindandmessage(pBlock, vAllErr, parameter.getWorkType(), pIgnoreRef, vInd);

				if (Objects.equals(pIgnoreRef, "N")) {

					// TODO SET_UPDATE_DCR(upper(p_block),v_record) --- Program Unit Calling
					setUpdateDcr(upper(pBlock), vRecord);

				}

				// TODO Set_Override_Button(p_block);
				coreptLib.setoverridebutton(pBlock);
				// TODO Set_Application_Property(cursor_style,"DEFAULT");

			}

			else {

				// TODO set_initial_error_display(P_BLOCK);
				coreptLib.setinitialerrordisplay(pBlock);
			}

			log.info("doValidate Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing doValidate" + e.getMessage());
			throw e;

		}
	}

	@Override
	public Map<String, Object> populateRecord(String pBlock) throws Exception {
		// public void populateRecord(String pBlock,Integer pCycle,Object pRecord)
		// throws Exception{
		log.info("populateRecord Executing");
		Map<String, Object> res = new HashMap<>();
		// String query = "";
		// Record rec = null;
		Integer pCycle = null;
		CrRunway pRecord = new CrRunway();
		try {
			// TODO Configure the Out Params --> p_record
			// TODO Configure the Out Params --> p_cycle

//			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_SR")) {
//				pRecord.setRecordType(parameter.getRecordType());
//				pRecord.setAirportIcao(PlStdRunwaySr.getAirportIcao());
//				pRecord.setAirportIdent(PlStdRunwaySr.getAirportIdent());
//				pRecord.setCustAreaCode(PlStdRunwaySr.getAreaCode());
//				pRecord.setCycleData(PlStdRunwaySr.getCycleData());
//				pRecord.setDisplacedThresholdDistance(
//						lpad(toChar(PlStdRunwaySr.getDisplacedThresholdDistance()), 4, '0'));
//				pRecord.setFileRecno(lpad(toChar(PlStdRunwaySr.getFileRecno()), 5, '0'));
//				pRecord.setLandingThresholdElevation(toChar(PlStdRunwaySr.getLandingThresholdElevation()));
//				pRecord.setLocalizerMlsClass(PlStdRunwaySr.getLocalizerMlsClass());
//				pRecord.setLocalizerMlsClass(PlStdRunwaySr.getLocalizerMlsGlsIdent());
//				pRecord.setRunwayDescription(PlStdRunwaySr.getRunwayDescription());
//				pRecord.setRunwayGradient(rpad(PlStdRunwaySr.getRunwayGradient(), 5, " "));
//				pRecord.setRunwayIdent(rpad(PlStdRunwaySr.getRunwayIdent(), 5, " "));
//				pRecord.setRunwayLatitude(PlStdRunwaySr.getRunwayLatitude());
//				pRecord.setRunwayLength(lpad(toChar(PlStdRunwaySr.getRunwayLength()), 5, '0'));
//				pRecord.setRunwayLongitude(PlStdRunwaySr.getRunwayLongitude());
//				pRecord.setRunwayMagneticBearing(PlStdRunwaySr.getRunwayMagneticBearing());
//				pRecord.setRunwayWidth(lpad(toChar(PlStdRunwaySr.getRunwayWidth()), 3, '0'));
//				pRecord.setSecondLocalizerClass(PlStdRunwaySr.getSecondLocalizerClass());
//				pRecord.setLocalizerMlsGlsIdent(PlStdRunwaySr.getSecondLocalizerMlsGlsIdent());
//				pRecord.setStopway(lpad(toString(PlStdRunwaySr.getStopway()), 4, '0'));
//				pRecord.setThresholdCrossingHeight(lpad(toChar(PlStdRunwaySr.getThresholdCrossingHeight()), 2, '0'));
//				pCycle = PlStdRunwaySr.getProcessingCycle();
//				pRecord.setRunwayAccuracyCompInd(PlStdRunwaySr.getRunwayAccuracyCompInd());
//				pRecord.setLandThreselevAccrCompInd(PlStdRunwaySr.getLandThreselevAccrCompInd());
//
//			}

			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_MR")) {
				pRecord.setRecordType(parameter.getRecordType());
				pRecord.setAirportIcao(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pRecord.setAirportIdent(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pRecord.setCustAreaCode(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAreaCode());
				pRecord.setCycleData(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getCycleData());
				pRecord.setDisplacedThresholdDistance(lpad(
						toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getDisplacedThresholdDistance()), 4,
						'0'));
				pRecord.setFileRecno(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getFileRecno()), 5, '0'));
				pRecord.setLandingThresholdElevation(
						toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLandingThresholdElevation()));
				pRecord.setLocalizerMlsClass(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pRecord.setLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pRecord.setRunwayDescription(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayDescription());
				pRecord.setRunwayGradient(
						rpad(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayGradient(), 5, " "));
				pRecord.setRunwayIdent(
						rpad(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent(), 5, " "));
				pRecord.setRunwayLatitude(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLatitude());
				pRecord.setRunwayLength(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLength()), 5, '0'));
				pRecord.setRunwayLongitude(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLongitude());
				pRecord.setRunwayMagneticBearing(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());
				pRecord.setRunwayWidth(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayWidth()), 3, '0'));
				pRecord.setSecondLocalizerClass(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pRecord.setSecondLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				pRecord.setStopway(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getStopway()), 4, '0'));
				pRecord.setThresholdCrossingHeight(
						lpad(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getThresholdCrossingHeight()),
								2, '0'));
				pCycle = toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle());
				pRecord.setRunwayAccuracyCompInd(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayAccuracyCompInd());
				pRecord.setLandThreselevAccrCompInd(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLandThreselevAccrCompInd());

			}

//			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_SR")) {
//				pRecord.setRecordType(parameter.getRecordType());
//				pRecord.setAirportIcao(PlTldRunwaySr.getAirportIcao());
//				pRecord.setAirportIdent(PlTldRunwaySr.getAirportIdent());
//				pRecord.setCustAreaCode(PlTldRunwaySr.getCustomerIdent());
//				pRecord.setCycleData(PlTldRunwaySr.getCycleData());
//				pRecord.setDisplacedThresholdDistance(
//						lpad(toChar(PlTldRunwaySr.getDisplacedThresholdDistance()), 4, '0'));
//				pRecord.setFileRecno(lpad(toChar(PlTldRunwaySr.getFileRecno()), 5, '0'));
//				pRecord.setLandingThresholdElevation(toChar(PlTldRunwaySr.getLandingThresholdElevation()));
//				pRecord.setLocalizerMlsClass(PlTldRunwaySr.getLocalizerMlsClass());
//				pRecord.setLocalizerMlsGlsIdent(PlTldRunwaySr.getLocalizerMlsGlsIdent());
//				pRecord.setRunwayDescription(PlTldRunwaySr.getRunwayDescription());
//				pRecord.setRunwayGradient(rpad(PlTldRunwaySr.getRunwayGradient(), 5, " "));
//				pRecord.setRunwayIdent(rpad(PlTldRunwaySr.getRunwayIdent(), 5, " "));
//				pRecord.setRunwayLatitude(PlTldRunwaySr.getRunwayLatitude());
//				pRecord.setRunwayLength(lpad(toChar(PlTldRunwaySr.getRunwayLength()), 5, '0'));
//				pRecord.setRunwayLongitude(PlTldRunwaySr.getRunwayLongitude());
//				pRecord.setRunwayMagneticBearing(PlTldRunwaySr.getRunwayMagneticBearing());
//				pRecord.setRunwayWidth(lpad(toChar(PlTldRunwaySr.getRunwayWidth()), 3, '0'));
//				pRecord.setSecondLocalizerClass(PlTldRunwaySr.getSecondLocalizerClass());
//				pRecord.setSecondLocalizerMlsGlsIdent(PlTldRunwaySr.getSecondLocalizerMlsGlsIdent());
//				pRecord.setStopway(lpad(toChar(PlTldRunwaySr.getStopway()), 4, '0'));
//				pRecord.setThresholdCrossingHeight(lpad(toChar(PlTldRunwaySr.getThresholdCrossingHeight()), 2, '0'));
//				pCycle = PlTldRunwaySr.getProcessingCycle();
//				pRecord.setRunwayAccuracyCompInd(PlTldRunwaySr.getRunwayAccuracyCompInd());
//				pRecord.setLandThreselevAccrCompInd(PlTldRunwaySr.getLandThreselevAccrCompInd());
//
//			}

			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_MR")) {
				pRecord.setRecordType(parameter.getRecordType());
				pRecord.setAirportIcao(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pRecord.setAirportIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pRecord.setCustAreaCode(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCustomerIdent());
				pRecord.setCycleData(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCycleData());
				pRecord.setDisplacedThresholdDistance(lpad(
						toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getDisplacedThresholdDistance()), 4,
						'0'));
				pRecord.setFileRecno(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getFileRecno()), 5, '0'));
				pRecord.setLandingThresholdElevation(
						toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLandingThresholdElevation()));
				pRecord.setLocalizerMlsClass(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pRecord.setLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pRecord.setRunwayDescription(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayDescription());
				pRecord.setRunwayGradient(
						rpad(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayGradient(), 5, " "));
				pRecord.setRunwayIdent(
						rpad(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent(), 5, " "));
				pRecord.setRunwayLatitude(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLatitude());
				pRecord.setRunwayLength(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLength()), 5, '0'));
				pRecord.setRunwayLongitude(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayLongitude());
				pRecord.setRunwayMagneticBearing(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());
				pRecord.setRunwayWidth(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayWidth()), 3, '0'));
				pRecord.setSecondLocalizerClass(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pRecord.setSecondLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				pRecord.setStopway(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getStopway()), 4, '0'));
				pRecord.setThresholdCrossingHeight(
						lpad(toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getThresholdCrossingHeight()),
								2, '0'));
				pCycle = toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle());
				pRecord.setRunwayAccuracyCompInd(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayAccuracyCompInd());
				pRecord.setLandThreselevAccrCompInd(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLandThreselevAccrCompInd());

			}
			res.put("pRecord", pRecord);
			res.put("pCycle", pCycle);

			log.info("populateRecord Executed Successfully");
			return res;
		} catch (Exception e) {
			log.error("Error while executing populateRecord" + e.getMessage());
			throw e;

		}
	}

	// TODO PUnits Manual configuration
	// ParentName ---> INITIALIZE_FORM
	// File Name ---> corept_template.fmb
	// TODO PUnits Manual configuration
	// ParentName ---> POPULATE_ITEMS
	// File Name ---> airport.fmb

	@Override
	public Map<String, Object> populateRelRecord(String pBlock) throws Exception {
		log.info("populateRelRecord Executing");
		Map<String, Object> res = new HashMap<>();
		PlStdRunway pSrec = new PlStdRunway();
		PlTldRunway pTrec = new PlTldRunway();
		try {
			// TODO Configure the Out Params --> p_srec
			// TODO Configure the Out Params --> p_trec

//			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_SR")) {
//				pSrec.setAirportIcao(PlStdRunwaySr.getAirportIcao());
//				pSrec.setAirportIdent(PlStdRunwaySr.getAirportIdent());
//				pSrec.setRunwayIdent(PlStdRunwaySr.getRunwayIdent());
//				pSrec.setValidateInd(PlStdRunwaySr.getValidateInd());
//				pSrec.setLocalizerMlsGlsIdent(PlStdRunwaySr.getLocalizerMlsGlsIdent());
//				pSrec.setSecondLocalizerMlsGlsIdent(PlStdRunwaySr.getSecondLocalizerMlsGlsIdent());
//				if (toInteger(global.getRecentCycle()) >= PlStdRunwaySr.getProcessingCycle()) {
//					pSrec.setProcessingCycle(PlStdRunwaySr.getProcessingCycle());
//
//				}
//
//				else {
//					pSrec.setProcessingCycle(toInteger(global.getRecentCycle()));
//
//				}
//				pSrec.setAreaCode(PlStdRunwaySr.getAreaCode());
//				pSrec.setDataSupplier(PlStdRunwaySr.getDataSupplier());
//				pSrec.setCreateDcrNumber(PlStdRunwaySr.getCreateDcrNumber());
//				pSrec.setLocalizerMlsClass(PlStdRunwaySr.getLocalizerMlsClass());
//				pSrec.setSecondLocalizerClass(PlStdRunwaySr.getSecondLocalizerClass());
//				pSrec.setRunwayMagneticBearing(PlStdRunwaySr.getRunwayMagneticBearing());
//
//			}

			if (Objects.equals(upper(pBlock), "PL_STD_RUNWAY_MR")) {
				pSrec.setAirportIcao(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pSrec.setAirportIdent(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pSrec.setRunwayIdent(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent());
				pSrec.setValidateInd(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getValidateInd());
				pSrec.setLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pSrec.setSecondLocalizerMlsGlsIdent(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				if (toInteger(global.getRecentCycle()) >= toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex())
						.getProcessingCycle())) {
					pSrec.setProcessingCycle(toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()));

				}

				else {
					pSrec.setProcessingCycle(toInteger(global.getRecentCycle()));

				}
				pSrec.setAreaCode(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getAreaCode());
				pSrec.setDataSupplier(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getDataSupplier());
				pSrec.setCreateDcrNumber(toInteger(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				pSrec.setLocalizerMlsClass(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pSrec.setSecondLocalizerClass(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pSrec.setRunwayMagneticBearing(
						plStdRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());

			}

//			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_SR")) {
//				pTrec.setAirportIcao(PlTldRunwaySr.getAirportIcao());
//				pTrec.setAirportIdent(PlTldRunwaySr.getAirportIdent());
//				pTrec.setRunwayIdent(PlTldRunwaySr.getRunwayIdent());
//				pTrec.setValidateInd(PlTldRunwaySr.getValidateInd());
//				pTrec.setLocalizerMlsGlsIdent(PlTldRunwaySr.getLocalizerMlsGlsIdent());
//				pTrec.setSecondLocalizerMlsGlsIdent(PlTldRunwaySr.getSecondLocalizerMlsGlsIdent());
//				pTrec.setCustomerIdent(PlTldRunwaySr.getCustomerIdent());
//				pTrec.setDataSupplier(PlTldRunwaySr.getDataSupplier());
//				pTrec.setGeneratedInHouseFlag(PlTldRunwaySr.getGeneratedInHouseFlag());
//				if (toInteger(global.getRecentCycle()) >= PlTldRunwaySr.getProcessingCycle()) {
//
//					pTrec.setProcessingCycle(PlTldRunwaySr.getProcessingCycle());
//
//				}
//
//				else {
//					pTrec.setProcessingCycle(toInteger(global.getRecentCycle()));
//
//				}
//				pTrec.setCreateDcrNumber(PlTldRunwaySr.getCreateDcrNumber());
//				pTrec.setLocalizerMlsClass(PlTldRunwaySr.getLocalizerMlsClass());
//				pTrec.setSecondLocalizerClass(PlTldRunwaySr.getSecondLocalizerClass());
//				pTrec.setRunwayMagneticBearing(PlTldRunwaySr.getRunwayMagneticBearing());
//
//			}

			else if (Objects.equals(upper(pBlock), "PL_TLD_RUNWAY_MR")) {
				pTrec.setAirportIcao(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pTrec.setAirportIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pTrec.setRunwayIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent());
				pTrec.setValidateInd(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getValidateInd());
				pTrec.setLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsGlsIdent());
				pTrec.setSecondLocalizerMlsGlsIdent(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerMlsGlsIdent());
				pTrec.setCustomerIdent(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCustomerIdent());
				pTrec.setDataSupplier(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getDataSupplier());
				pTrec.setGeneratedInHouseFlag(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getGeneratedInHouseFlag());
				pTrec.setCreateDcrNumber(toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				pTrec.setProcessingCycle(toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()));
				pTrec.setLocalizerMlsClass(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getLocalizerMlsClass());
				pTrec.setSecondLocalizerClass(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getSecondLocalizerClass());
				pTrec.setRunwayMagneticBearing(
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayMagneticBearing());

			}

			res.put("pSrec", pSrec);
			res.put("pTrec", pTrec);

			log.info("populateRelRecord Executed Successfully");
			return res;
		} catch (Exception e) {
			log.error("Error while executing populateRelRecord" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String checkRunwayRef(String pTable, String pRunwayIdent, Integer pCycle, String pCust, String pDcr)
			throws Exception {
		log.info("checkRunwayRef Executing");
		String query = "";
		Record rec = null;
		try {
			Integer vCount = null;
			String vStmt = null;
			// Integer vDcrNumber = 0;
			String vReferencingTable = null;
			String vBRunway = "N";
			String vType = null;
			String vBlock = system.getCursorBlock();
			String vAirportIdent = toChar(nameIn(this, vBlock + ".AIRPORT_IDENT"));
			String vAirportIcao = toChar(nameIn(this, vBlock + ".AIRPORT_ICAO"));
			String getRefInfo = """
					select unique lower(REFERENCING_TABLE_NAME)
					  	from ref_table
					   	where data_supplier = ?
					   	and processing_cycle = ?
					   	and DCR_NUMBER = ?
					  	and (lower(REFERENCING_TABLE_NAME) like '%sid%'
					  	or lower(REFERENCING_TABLE_NAME) like '%star%'
					  	or lower(REFERENCING_TABLE_NAME) like '%approach%')
					""";

			query = """
					select count(*) from ref_table
					  where data_supplier = ?
					  and processing_cycle = ?
					  and DCR_NUMBER = ?
					  and lower(REFERENCING_TABLE_NAME) not like '%sid%'
					  and lower(REFERENCING_TABLE_NAME) not like '%star%'
					  and lower(REFERENCING_TABLE_NAME) not like '%approach%'
					""";
			rec = app.selectInto(query, global.getDataSupplier(), pCycle, pDcr);
			vCount = rec.getInt();
			if (vCount > 0) {

				return "no";

			}

			else {
				List<Record> records = app.executeQuery(getRefInfo, global.getDataSupplier(), pCycle, pDcr);

				for (Record getRef : records) {

					vReferencingTable = toString(getRef.getObject());

					if (like("%sid%", vReferencingTable)) {
						vType = "sid";

					}

					else if (like("%star%", vReferencingTable)) {
						vType = "star";

					}

					if (like("%approach%", vReferencingTable)) {
						vStmt = "select 'Y' from " + vReferencingTable + " " + "where data_Supplier = '"
								+ global.getDataSupplier() + "' " + "and   processing_cycle = '" + pCycle + "' "
								+ "and 'RW'||approach_ident = " + substr(pRunwayIdent, 1, 4) + "B' "
								+ "and validate_ind in ('Y','S','H')";

					}

					else if (like("%heli%", vReferencingTable)) {
						vStmt = "select 'Y' from " + vReferencingTable + "_segment a, " + vReferencingTable + " b "
								+ "where a.data_Supplier = '" + global.getDataSupplier() + "' "
								+ "and   a.processing_cycle = '" + pCycle + "' " + "and   a.transition_ident = '"
								+ substr(pRunwayIdent, 1, 4) + "B' " + "and   b.validate_ind in ('Y','S','H') "
								+ "and   a.data_supplier = b.data_supplier "
								+ "and   a.processing_cycle = b.processing_cycle " + "and   a." + vType + "_ident = b."
								+ vType + "_ident " + "and   a.heliport_ident = b.heliport_ident "
								+ "and   a.heliport_icao = b.heliport_icao";

					}

					else {
						vStmt = "select 'Y' from " + vReferencingTable + "_segment a, " + vReferencingTable + " b "
								+ "where a.data_Supplier = '" + global.getDataSupplier() + "' "
								+ "and   a.processing_cycle = '" + pCycle + "' " + "and   a.transition_ident = '"
								+ substr(pRunwayIdent, 1, 4) + "B' " + "and   b.validate_ind in ('Y','S','H') "
								+ "and   a.data_supplier = b.data_supplier "
								+ "and   a.processing_cycle = b.processing_cycle " + "and   a." + vType + "_ident = b."
								+ vType + "_ident " + "and   a.airport_ident = b.airport_ident "
								+ "and   a.airport_icao = b.airport_icao";

					}
					// TODO v_B_runway := forms_utilities.get_statement_result(v_stmt)

					String dbCall = app.executeFunction(String.class, "CPTS", "get_statement_result", "forms_utilities",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_stmt", vStmt, OracleTypes.VARCHAR));
					vBRunway = dbCall;

					if (Objects.equals(vBRunway, "Y")) {
						if (Objects.equals(vCount, 0)) {
							if (Objects.equals(parameter.getRecordType(), "S")) {

								query = """
										select count(*) from pl_std_runway
																where data_supplier = ?
																and processing_cycle = ?
																and runway_ident like ?||'%'
																and runway_ident != ?
																and airport_ident = ?
																and airport_icao = ?
										""";
								rec = app.selectInto(query, global.getDataSupplier(), pCycle,
										substr(pRunwayIdent, 1, 4),
										pRunwayIdent, vAirportIdent, vAirportIcao);
								vCount = rec.getInt();

							} else {

								query = """
										select count(*) from pl_tld_runway
																where data_supplier = ?
																and processing_cycle = ?
																and customer_ident = ?
																and runway_ident like ?||'%'
																and runway_ident != ?
																and airport_ident = ?
																and airport_icao = ?
										""";
								rec = app.selectInto(query, global.getDataSupplier(), pCycle, pCust,
										substr(pRunwayIdent, 1, 4),
										pRunwayIdent, vAirportIdent, vAirportIcao);
								vCount = rec.getInt();

							}
							if (vCount > 1) {
							} else {
								return "no";
							}
						}
					} else {

						return "no";
					}
				}
				// TODO closeget_ref_info
			}

			log.info("checkRunwayRef Executed Successfully");
			return "ok";

		} catch (Exception e) {
			log.error("Error while executing checkRunwayRef" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void setUpdateDcr(String pBlock, CrRunway pRecord) throws Exception {
		log.info("setUpdateDcr Executing");
		//HashUtils hu = new HashUtils();
		// Record rec = null;
		try {
			String getRecCur = "";
			PlStdRunwayMr rstdRunway = new PlStdRunwayMr();
			PlTldRunwayMr rtldRunway = new PlTldRunwayMr();
log.debug("The default value is :"+ rstdRunway);
			Integer vDcr = toInteger(
					nameIn(this, pBlock + ".CREATE_DCR_NUMBER"));
			String vCycleData = substr(toChar(
					nameIn(this, pBlock + ".PROCESSING_CYCLE")),
					3);
			Integer vCount = 0;

			if (like("PL_STD%", pBlock)) {
				// TODO openget_rec_curforselect*frompl_std_runwaywherecreate_dcr_number=v_dcr
				getRecCur = """
						select * from pl_std_runway
							           where create_dcr_number = ?
						""";
				List<Record> cur = app.executeQuery(getRecCur, vDcr);

				for (Record getRec : cur) {
					// TODO fetchget_rec_curintorstd_runway
					rstdRunway = app.mapResultSetToClass(getRec, PlStdRunwayMr.class);
					// break;
					vCount = vCount + 1;
					if (!Objects.equals(nvl(pRecord.getCustAreaCode(), "-"), nvl(rstdRunway.getAreaCode(), "-"))
							|| !Objects.equals(nvl(pRecord.getDisplacedThresholdDistance(), 0),
									nvl(lpad(toChar(rstdRunway.getDisplacedThresholdDistance()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getLandingThresholdElevation(), 0),
									nvl(rstdRunway.getLandingThresholdElevation(), 0))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsClass(), "-"),
									nvl(rstdRunway.getLocalizerMlsClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsGlsIdent(), "-"),
									nvl(rstdRunway.getLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayDescription(), "-"),
									nvl(rstdRunway.getRunwayDescription(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayGradient(), "-"),
									nvl(rpad(rstdRunway.getRunwayGradient(), 5, " "), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLatitude(), "-"),
									nvl(rstdRunway.getRunwayLatitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLength(), 0),
									nvl(lpad(toChar(rstdRunway.getRunwayLength()), 5, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getRunwayLongitude(), "-"),
									nvl(rstdRunway.getRunwayLongitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayMagneticBearing(), "-"),
									nvl(rstdRunway.getRunwayMagneticBearing(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayWidth(), 0),
									nvl(lpad(toChar(rstdRunway.getRunwayWidth()), 3, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerClass(), "-"),
									nvl(rstdRunway.getSecondLocalizerClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerMlsGlsIdent(), "-"),
									nvl(rstdRunway.getSecondLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayAccuracyCompInd(), "-"),
									nvl(rstdRunway.getRunwayAccuracyCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getLandThreselevAccrCompInd(), "-"),
									nvl(rstdRunway.getLandThreselevAccrCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getStopway(), 0),
									nvl(lpad(toChar(rstdRunway.getStopway()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getThresholdCrossingHeight(), 0),
									nvl(lpad(toChar(rstdRunway.getThresholdCrossingHeight()), 2, '0'), 0))) {
						copy(this, toInteger(global.getDcrNumber()), pBlock + ".update_Dcr_Number");
						copy(this, vCycleData, pBlock + ".cycle_Data");

					}
				}
				//// TODO closeget_rec_cur
				//
			}

			else if (like("PL_TLD%", pBlock)) {
				// TODO openget_rec_curforselect*frompl_tld_runwaywherecreate_dcr_number=v_dcr
				getRecCur = """
						select * from pl_tld_runway
							           where create_dcr_number = ?
						""";
				List<Record> cur = app.executeQuery(getRecCur, vDcr);

				for (Record getRec : cur) {
					// TODO fetchget_rec_curintorstd_runway
					rtldRunway = app.mapResultSetToClass(getRec, PlTldRunwayMr.class);

					vCount = vCount + 1;
					if (!Objects.equals(nvl(pRecord.getDisplacedThresholdDistance(), 0),
							nvl(lpad(toChar(rtldRunway.getDisplacedThresholdDistance()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getLandingThresholdElevation(), 0),
									nvl(rtldRunway.getLandingThresholdElevation(), 0))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsClass(), "-"),
									nvl(rtldRunway.getLocalizerMlsClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getLocalizerMlsGlsIdent(), "-"),
									nvl(rtldRunway.getLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayDescription(), "-"),
									nvl(rtldRunway.getRunwayDescription(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayGradient(), "-"),
									nvl(rpad(rtldRunway.getRunwayGradient(), 5, " "), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLatitude(), "-"),
									nvl(rtldRunway.getRunwayLatitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayLength(), 0),
									nvl(lpad(toChar(rtldRunway.getRunwayLength()), 5, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getRunwayLongitude(), "-"),
									nvl(rtldRunway.getRunwayLongitude(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayMagneticBearing(), "-"),
									nvl(rtldRunway.getRunwayMagneticBearing(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayWidth(), 0),
									nvl(lpad(toChar(rtldRunway.getRunwayWidth()), 3, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerClass(), "-"),
									nvl(rtldRunway.getSecondLocalizerClass(), "-"))
							|| !Objects.equals(nvl(pRecord.getSecondLocalizerMlsGlsIdent(), "-"),
									nvl(rtldRunway.getSecondLocalizerMlsGlsIdent(), "-"))
							|| !Objects.equals(nvl(pRecord.getRunwayAccuracyCompInd(), "-"),
									nvl(rtldRunway.getRunwayAccuracyCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getLandThreselevAccrCompInd(), "-"),
									nvl(rtldRunway.getLandThreselevAccrCompInd(), "-"))
							|| !Objects.equals(nvl(pRecord.getStopway(), 0),
									nvl(lpad(toChar(rtldRunway.getStopway()), 4, '0'), 0))
							|| !Objects.equals(nvl(pRecord.getThresholdCrossingHeight(), 0),
									nvl(lpad(toChar(rtldRunway.getThresholdCrossingHeight()), 2, '0'), 0))) {
						copy(this, toInteger(global.getDcrNumber()), pBlock + ".update_Dcr_Number");
						copy(this, vCycleData, pBlock + ".cycle_Data");

					}

				}
				//// TODO closeget_rec_cur
				//
			}

			if (Objects.equals(vCount, 0)) {

				copy(this, toInteger(global.getDcrNumber()), pBlock + ".update_Dcr_Number");
				copy(this, vCycleData, pBlock + ".cycle_Data");

			}

			log.info("setUpdateDcr Executed Successfully");
		}

		catch (Exception e) {
			log.error("Error while executing setUpdateDcr" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void deleteDcrNo() throws Exception {
		log.info("deleteDcrNo Executing");
		try {
			Integer lsLength = null;
			String lsDcr1 = null;
			String lsDcr2 = null;

			lsLength = instr("," + global.getNewDcrNo() + ",",
					"," + plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber() + ",");
			if (Objects.equals(lsLength, 1)) {
				lsDcr1 = null;

			}

			else {
				lsDcr1 = rtrim(ltrim(substr(global.getNewDcrNo(), 1, lsLength - 2), ", "), ", ");
			}
			lsDcr2 = rtrim(ltrim(
					substr(global.getNewDcrNo(),
							lsLength + 1 + length(
									plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())),
					", "), ", ");

			global.setNewDcrNo(ltrim(getNullClean(lsDcr1) + "," + getNullClean(lsDcr2), ", "));
			global.setNewDcrNo(ltrim(global.getNewDcrNo(), ","));
			log.info("deleteDcrNo Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing deleteDcrNo" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> onMessage(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onMessage Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// Integer msgnum = messageCode;
			// String msgtxt = messageText;
			// String msgtyp = messageType;

			// TODO Set_Application_Property(cursor_style,"DEFAULT");
			// if((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) ||
			// Objects.equals(msgnum, 40407))) {

			// TODO CLEAR_MESSAGE;
			// clearMessage();
			// parameter.setUpdRec("N");
			// setBlockProperty(toChar(nameIn(this,"system.cursor_block")),
			// FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_TRUE);
			// message("record has been saved successfully");
			//
			// }
			//
			//
			// else if(Arrays.asList(41051,40350,47316,40353).contains(msgnum)) {
			// null;
			//
			// }
			//
			//
			// else if(Objects.equals(msgnum, 41050) &&
			// Objects.equals(parameter.getWorkType(), "VIEW")) {
			// null;
			//
			// }
			//
			//
			// else if(Arrays.asList(40401,40405).contains(msgnum)) {
			// null;
			//
			// }
			//
			//
			// else if(Objects.equals(msgnum, 40352)) {
			// message("last record retrieved.");
			//
			// }
			//
			//
			// else {
			//
			// //TODO display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
			// "||msgtxt);
			// throw new FormTriggerFailureException();
			//
			// }
			// OracleHelpers.ResponseMapper(this, resDto);
			log.info(" onMessage executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the onMessage Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> onError(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onError Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer msgnum = global.getErrorCode();// errorCode;
			String msgtxt = "";// errorText;
			// String msgtyp = null;// errorType;
			String vBlockName = system.getCursorBlock();

			// TODO Set_Application_Property(cursor_style,"DEFAULT");
			if ((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) || Objects.equals(msgnum, 40407))) {
				message("changes saved successfully");

			}

			else if (Arrays.asList(41051, 40350, 47316, 40353).contains(msgnum)) {
				// null;
			}

			else if (Objects.equals(msgnum, 41050) && Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;
			}

			else if (Arrays.asList(40401, 40405).contains(msgnum)) {
				// null;
			}

			else if (Objects.equals(msgnum, 40100)) {

				// TODO clear_message;
				clearMessage();
				message("at the first record.");
			}

			else if (Objects.equals(msgnum, 40735) && like("%01031%", msgtxt)) {

				// TODO clear_message;
				clearMessage();
				coreptLib.dspMsg(msgtxt + " Insufficient privileges. ");
				// TODO dsp_msg(msgtxt||" Insufficient privileges. ");

			}

			else if (Arrays.asList(40508, 40509).contains(msgnum)) {

				// TODO dsp_msg(msgtxt||chr(10)||chr(10)||"Please check the exact error message
				// from the "Display Error" in the "HELP" menu");

			}

			else if (Arrays.asList(40200).contains(msgnum)) {
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
						if (Objects.equals(parameter.getRecordType(), "T")) {

							// TODO
							// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,v_block_name||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
							coreptLib.dspActionMsg("U", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcr_number")),
									toInteger(nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")),
											global.getProcessingCycle())),
									toChar(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));
						}

						else {

							// TODO
							// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,v_block_name||".processing_cycle"),global.getProcessingCycle()));
							coreptLib.dspActionMsg("U", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcr_number")),
									toInteger(nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")),
											global.getProcessingCycle())),
									null);

						}

					}

					else {

						// TODO dsp_msg(msgtxt);
						coreptLib.dspMsg(msgtxt.equals("")? "Field is protected against update." : msgtxt);
						throw new FormTriggerFailureException();

					}

				}

				else {

					// TODO dsp_msg(msgtxt);
					coreptLib.dspMsg(msgtxt);
					throw new FormTriggerFailureException();

				}

			}

			else if (Objects.equals(msgnum, 41050) && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					// null;

				}

				else {

					// TODO dsp_msg(msgtxt);
					coreptLib.dspMsg(msgtxt);
					throw new FormTriggerFailureException();

				}

			}

			else {

				// TODO display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
				// "||msgtxt);

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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenNewFormInstance(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenNewFormInstance Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Initialize_Form --- Program Unit Calling
			OracleHelpers.bulkClassMapper(this, coreptTemplate);
			coreptTemplate.initializeForm();
			global.setNewDcrNo("");
			// hideView("stdTldFixStk");

			// TODO set_block;
			coreptLib.setBlock();
			// TODO if_from_error_summary;
			coreptLib.iffromerrorsummary();
			RecordGroup groupId = null;
			// RecordGroupColumn colId = null;
			String vGroup = "newDcr";

			groupId = findGroup("newDcr");
			if (groupId != null) {
				deleteGroup(groups, "newDcr");
			}

			else {
				groupId = createGroup(vGroup);
				// colId = addGroupColumn(groupId, "dcrNo", "numberColumn");

				// coverity-fixes
				addGroupColumn(groupId, "dcrNo", "numberColumn");
			}

			String queryHits = toString(plTldRunwayMr.getQueryHits());
			OracleHelpers.ResponseMapper(this, reqDto);
			whenNewRecordInstance(reqDto);
			plTldRunwayMr.setQueryHits(queryHits);
			if (!system.getFormStatus().equals("NEW")) {
              global.setCreateDcrNumber(toString(
                      nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
          }
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenNewRecordInstance(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenNewRecordInstance Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			String pBlock = system.getCursorBlock();
			Integer vAllowUpdate = 0;
			OracleHelpers.bulkClassMapper(this, coreptLib);
			if (like("PL_%", pBlock)) {
				if (Objects.equals(system.getMode(), "NORMAL")
						&& !Objects.equals(nvl(toChar(nameIn(this, pBlock + ".VALIDATE_IND")), "N"), "Y")
						&& !Objects.equals(toChar(nameIn(this, pBlock + ".PROCESSING_CYCLE")), null)) {

					// TODO do_validate(p_block) --- Program Unit Calling
					doValidate(pBlock, "Y");

				} else {
					OracleHelpers.bulkClassMapper(this, coreptLib);
					coreptLib.setinitialerrordisplay(pBlock);
				}
				if (!Objects.equals(parameter.getWorkType(), "VIEW")
						&& Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "N")) {
						setBlockItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						if (!like("%MR", pBlock)) {
							setBlockItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "notUpdatable");
							setBlockItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute",
									"notUpdatable");
							setBlockItemProperty(pBlock + ".validate_ind", "current_record_attribute", "default");
						}

					}

					else {
						setBlockItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						if (!like("%MR", pBlock)) {
							setBlockItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute", "default");
							setBlockItemProperty(pBlock + ".validate_ind", "current_record_attribute", "notUpdatable");
						}

						if (Objects.equals(global.getLibRefreshed(), "Y")
								&& Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "Y")) {
							setBlockItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
									FormConstant.PROPERTY_FALSE);
							if (!like("%MR", pBlock)) {
								setBlockItemProperty(pBlock + ".processing_cycle", "current_record_attribute",
										"notUpdatable");
							}
						} else {
							setBlockItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
									FormConstant.PROPERTY_TRUE);
							if (!like("%MR", pBlock)) {
								setBlockItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "default");

							}

						}

					}

				}

				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(toChar(nameIn(this, pBlock + ".processing_Cycle")), null)) {
						vAllowUpdate = toInteger(coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, pBlock + ".customer_Ident"))));

					} else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, pBlock + ".processing_Cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, pBlock + ".customer_Ident")));

					}
				} else {
					if (Objects.equals(nameIn(this, pBlock + ".processing_Cycle"), null)) {
						vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);

					} else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, pBlock + ".processing_Cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);
					}

				}
				if (Objects.equals(vAllowUpdate, 1)) {
					parameter.setUpdRec("N");
					setBlockProperty(pBlock, FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_TRUE);

				} else {
					parameter.setUpdRec("Y");
					setBlockProperty(pBlock, FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_FALSE);
					if (!Objects.equals(toChar(nameIn(this, pBlock + ".processing_Cycle")),
							global.getProcessingCycle())) {
						throw new FormTriggerFailureException(event);

					}

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyExeqry(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExeqry Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				checkToCommit("EXECUTE_QUERY", reqDto);
				system.setFormStatus("NORMAL");
				if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR"))
					this.getPlStdRunwayMr().add(0, new PlStdRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR"))
					this.getPlTldRunwayMr().add(0, new PlTldRunwayMr());
				system.setCursorRecordIndex(0);
			}
			else if (Objects.equals(system.getMode(), "NORMAL") && Objects.equals(parameter.getWorkType(), "VIEW")) {
				system.setFormStatus("NORMAL");
				if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR"))
					this.getPlStdRunwayMr().add(0, new PlStdRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR"))
					this.getPlTldRunwayMr().add(0, new PlTldRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "STD_RUNWAY_MR"))
					this.getStdRunwayMr().add(0, new StdRunwayMr());
				if (Objects.equals(system.getCursorBlock(), "TLD_RUNWAY_MR"))
					this.getTldRunwayMr().add(0, new TldRunwayMr());
				system.setCursorRecordIndex(0);
			}

			preQueryExecute();

			controlBlock.setChkUnchkAll("N");
			global.setNewDcrNo("");
			
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyExeqry executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyExeqry Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	private void preQueryExecute() throws Exception {

		if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR")) {
			PlStdRunwayMrPreQuery();
		} else if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR")) {
			PlTldRunwayMrPreQuery();
		} else if (Objects.equals(system.getCursorBlock(), "TLD_RUNWAY_MR")) {
			tldRunwayMrPreQuery();
		} else if (Objects.equals(system.getCursorBlock(), "STD_RUNWAY_MR")) {
			stdRunwayMrPreQuery();
		}

		coreptLib.coreptexecutequery(this);

		if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR")) {
			for (PlTldRunwayMr PlTldRunwayMr : plTldRunwayMr.getData()) {
				PlTldRunwayMr.setOldProcessingCycle(toInteger(PlTldRunwayMr.getProcessingCycle()));
			}
		}

		if (!system.getFormStatus().equals("NEW")) {
			global.setCreateDcrNumber(toString(nameIn(this,
					HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
		}

	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyEntqry(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyEntqry Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {

				// TODO check_to_commit('ENTER_QUERY') --- Program Unit Calling
				checkToCommit("ENTER_QUERY", reqDto);

			}

			coreptLib.coreptenterquery();
			system.setMode("ENTER_QUERY");
			system.setFormStatus("NORMAL");
			controlBlock.setChkUnchkAll("N");
			global.setNewDcrNo("");
			if (Objects.equals(system.getMode(), "NORMAL")) {

				// TODO unset_query_menu_items;
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> preInsert(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" preInsert Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String query = "";
			Record rec = null;
			String vValidateInd = null;
			Integer vDcrNumber = null;
			Integer vProcessingCycle = null;
			String vBlockName = toSnakeCase(system.getCursorBlock());
			Integer vAllowUpdate = 0;

			coreptLib.checkwildcardforkeys(vBlockName);

			if (Arrays.asList("CHANGED", "INSERT").contains(system.getRecordStatus())) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_Cycle"), null)) {
						vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, vBlockName + ".customer_ident")));

					}

					else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, vBlockName + ".processing_cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(),
								toChar(nameIn(this, vBlockName + ".customer_ident")));

					}

				}

				else {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);

					}

					else {
						vAllowUpdate = coreptLib.checkValidSuppCust(
								toInteger(nameIn(this, vBlockName + ".processing_cycle")),
								toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
								global.getDataSupplier(), parameter.getRecordType(), null);

					}

				}

			}

			if (Objects.equals(vAllowUpdate, 1)) {
				copy(this, global.getDataSupplier(), vBlockName + ".data_Supplier");
				copy(this, 0, vBlockName + ".file_Recno");

				query = """
						select dcr_number_seq.nextval from dual
						""";
				rec = app.selectInto(query);
				vDcrNumber = rec.getInt();
				copy(this, vDcrNumber, vBlockName + ".create_Dcr_Number");

				doValidate(vBlockName, "N");
				vValidateInd = toChar(nameIn(this, vBlockName + ".validate_ind"));
				vProcessingCycle = toInteger(
						nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")), global.getProcessingCycle()));
				if (Objects.equals(global.getLibRefreshed(), "Y")
						&& Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle()).contains(
								toChar(toChar(vProcessingCycle)))
						&& Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {

					refreshMasterLibrary.setRecordGroup(vDcrNumber, vValidateInd, vBlockName,
							vProcessingCycle, "I");
				}

			} else {
				if (Objects.equals(parameter.getRecordType(), "T")) {

					coreptLib.dspActionMsg("I", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, vBlockName + ".processing_cycle"), global.getProcessingCycle())),
							toChar(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));
				} else {
					coreptLib.dspActionMsg("I", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(toChar(nameIn(this, vBlockName + ".processing_cycle")),
									global.getProcessingCycle())),
							null);
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" preInsert executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the preInsert Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> preUpdate(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" preUpdate Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vBlockName = system.getCursorBlock();
			Integer vDcrNumber = toInteger(nameIn(this, vBlockName + ".create_dcr_number"));
			Integer vProcessingCycle = toInteger(nameIn(this, vBlockName + ".processing_cycle"));
			String vTable = toChar(nameIn(this, vBlockName + ".query_Data_Source_Name"));
			String vValidateIndNew = null;
			String vValidateIndOld = null;

			// TODO v_validate_ind_old :=
			// refresh_ml_utilities.get_validate_ind(v_table,v_dcr_number)
			String dbCall = app.executeFunction(String.class, "CPTM", "Get_validate_ind", "refresh_ml_utilities",
					OracleTypes.VARCHAR,
					new ProcedureInParameter("p_table", vTable, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_dcr", vDcrNumber, OracleTypes.NUMBER));
			vValidateIndOld = dbCall;

			if (Arrays.asList("Y", "S", "H", "O").contains(vValidateIndOld)) {

				refreshMasterLibrary.deleteFromRefTable(vDcrNumber, null);
			}

			doValidate(vBlockName, "N");
			vValidateIndNew = toChar(nameIn(this, vBlockName + ".validate_ind"));
			if (Arrays.asList("Y", "S", "H", "O").contains(vValidateIndOld)
					&& Arrays.asList("W", "N", "I").contains(vValidateIndNew)) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(
							toChar(checkRunwayRef("pl_tld_runway", toChar(nameIn(this, vBlockName + ".runway_ident")),
									toInteger(nameIn(this, vBlockName + ".processing_cycle")),
									toChar(nameIn(this, vBlockName + ".customer_ident")),
									toChar(nameIn(this, vBlockName + ".create_dcr_number")))),
							"no")) {
						if (Objects.equals(toChar(refreshMasterLibrary.checkReferenceInfo(vBlockName, null)), "N")) {
							throw new FormTriggerFailureException(event);
						}
					}
				}

				else {
					if (Objects.equals(
							toChar(checkRunwayRef("pl_std_runway", toChar(nameIn(this, vBlockName + ".runway_ident")),
									toInteger(nameIn(this, vBlockName + ".processing_cycle")), null,
									toChar(nameIn(this, vBlockName + ".create_dcr_number")))),
							"no")) {
						if (Objects.equals(toChar(refreshMasterLibrary.checkReferenceInfo(vBlockName, null)), "N")) {
							throw new FormTriggerFailureException(event);
						}
					}
				}
			}

			if (Objects.equals(global.getLibRefreshed(), "Y")
					&& Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle())
							.contains(toChar(vProcessingCycle))) {
				if (Arrays.asList("Y", "S", "H", "O").contains(vValidateIndNew)
						|| Arrays.asList("Y", "S", "H", "O").contains(vValidateIndOld)) {

					refreshMasterLibrary.setRecordGroup(vDcrNumber, vValidateIndNew, vBlockName, vProcessingCycle, "U");
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" preUpdate executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the preUpdate Service");
			throw e;
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyDelrec(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDelrec Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		Boolean isChecked = false;
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vAllowUpdate = 0;
			String lsBlockName = system.getCursorBlock();
			// Object obj =
			// OracleHelpers.findBlock(this,HoneyWellUtils.toCamelCase(system.getCursorBlock()));
			// Integer lnButton = 0;
			Integer lnDcrNo = null;
			String lsTableName = null;
			String lsFixIdent = null;
			String lsAirportIdent = null;
			String lsAirportIcao = null;
			Integer lnProcessingCycle = null;
			Integer vButton = 0;
			String lsRefInfo = null;

			lsFixIdent = toChar(nameIn(this, lsBlockName + ".runway_Ident"));
			lsAirportIdent = toChar(nameIn(this, lsBlockName + ".airport_Ident"));
			lsAirportIcao = toChar(nameIn(this, lsBlockName + ".airport_Icao"));
			lnProcessingCycle = toInteger(nameIn(this, lsBlockName + ".processing_Cycle"));
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			} else {
				if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					if (Objects.equals(parameter.getRecordType(), "T")) {
						if (Objects.equals(nameIn(this, lsBlockName + ".processing_Cycle"), null)) {
							vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(),
									toChar(nameIn(this, lsBlockName + ".customer_Ident")));

						} else {
							vAllowUpdate = coreptLib.checkValidSuppCust(
									toInteger(nameIn(this, lsBlockName + ".processing_Cycle")),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(),
									toChar(nameIn(this, lsBlockName + ".customer_Ident")));

						}
					} else {
						if (Objects.equals(nameIn(this, lsBlockName + ".processing_Cycle"), null)) {
							vAllowUpdate = coreptLib.checkValidSuppCust(toInteger(global.getProcessingCycle()),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(), null);

						} else {
							vAllowUpdate = coreptLib.checkValidSuppCust(
									toInteger(nameIn(this, lsBlockName + ".processing_Cycle")),
									toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
									global.getDataSupplier(), parameter.getRecordType(), null);

						}
					}
					if (Objects.equals(vAllowUpdate, 1)) {
						if(!Arrays.asList("NEW","INSERT").contains(system.getRecordStatus())) {
						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
							vButton = moreButtons("S", "Delete Record",
									"You are going to delete this record. Please be sure." + "\n" + " ", "Delete It",
									"Cancel", "");
							alertDetails.createNewRecord("delRec");
							throw new AlertException(event, alertDetails);
						} else {
							vButton = alertDetails.getAlertValue("delRec", alertDetails.getCurrentAlert());
						}

                        if (Objects.equals(vButton, 1)) {

                          String pTableType = "M2C";
                          String lsBlockName1 = system.getCursorBlock();
                          // String pTableName = ltrim(upper(getBlockProperty(lsBlockName,
                          // queryDataSourceName)), "PL_");
                          String pTableName = ltrim(
                              upper(toChar(nameIn(this, lsBlockName + ".query_Data_Source_Name"))),
                              "PL_");
                          Integer vDcrNumber = toInteger(
                              nameIn(this, lsBlockName1 + ".create_Dcr_Number"));
                          Integer vProcessingCycle = toInteger(
                              nameIn(this, lsBlockName1 + ".processing_Cycle"));
                          String vValidateInd = toChar(
                              nameIn(this, lsBlockName1 + ".validate_Ind"));
                          String vStatus = system.getRecordStatus();
                          String blkName = toChar(
                              nameIn(this, lsBlockName1 + ".query_Data_Source_Name"));
                          // String dcrNumber = toChar(nameIn(this, lsBlockName1 +
                          // ".create_Dcr_Number"));

                          if (Objects.equals(vStatus, "CHANGED")) {
                            // TODO v_validateInd :=
                            // refresh_ml_utilities.Get_validate_ind(get_block_property(ls_Block_Name,QUERY_DATA_SOURCE_NAME),name_in(ls_Block_Name||'.createDcrNumber'))
                            String dbCall = app.executeFunction(String.class, "CPTM",
                                "Get_validate_ind", "refresh_ml_utilities", OracleTypes.VARCHAR,
                                new ProcedureInParameter("p_table", blkName, OracleTypes.VARCHAR),
                                new ProcedureInParameter("p_dcr", vDcrNumber, OracleTypes.NUMBER));
                            vValidateInd = dbCall;

                          }

                          if (Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {
                            if (Objects.equals(parameter.getRecordType(), "T")) {
                              if (Objects
                                  .equals(
                                      toChar(checkRunwayRef("pl_tld_runway",
                                          toChar(nameIn(this, lsBlockName1 + ".runway_Ident")),
                                          toInteger(
                                              nameIn(this, lsBlockName1 + ".processing_Cycle")),
                                          toChar(nameIn(this, lsBlockName1 + ".customer_Ident")),
                                          toChar(
                                              nameIn(this, lsBlockName1 + ".create_Dcr_Number")))),
                                      "no")) {
                                if (Objects.equals(
                                    toChar(
                                        refreshMasterLibrary.checkReferenceInfo(lsBlockName1, "D")),
                                    "N")) {

                                  // TODO
                                  // Forms_Utilities.Du_Std_Tld_Fix_Prc(ls_Fix_Ident,NULL,ls_Airport_Ident,ls_Airport_Icao,"P","G",global.getDataSupplier(),ln_processingCycle,ln_Dcr_No,ls_Table_Name);

                                  Map<String, Object> dbCall = app.executeProcedure("CPTS",
                                      "Du_Std_Tld_Fix_Prc", "Forms_Utilities",
                                      new ProcedureInParameter("pi_Fix_Ident", lsFixIdent,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Fix_Icao", null,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Airport_Ident", lsAirportIdent,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Airport_Icao", lsAirportIcao,
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Sec", "P", OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Sub_Sec", "G",
                                          OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_Data_Supplier",
                                          global.getDataSupplier(), OracleTypes.VARCHAR),
                                      new ProcedureInParameter("pi_processingCycle",
                                          lnProcessingCycle, OracleTypes.NUMBER),
                                      new ProcedureOutParameter("lnDcrNo", OracleTypes.NUMBER),
                                      new ProcedureOutParameter("lsTableName",
                                          OracleTypes.VARCHAR));

                                  BigDecimal bg = OracleHelpers.toBigDecimal(dbCall.get("lnDcrNo"));
                                  if (!Objects.equals(bg, null)) {
                                    lnDcrNo = bg.intValue();
                                  }
                                  lsTableName = toString(dbCall.get("lsTableName"));
                                  lsRefInfo = "N";
                                  // showView("stdTldFixStk");
                                  // hideView("stdTldFixStk");

                                }
                              }
                            }

                            else {
                              if (Objects
                                  .equals(
                                      toChar(checkRunwayRef("pl_std_runway",
                                          toChar(nameIn(this, lsBlockName1 + ".runway_Ident")),
                                          toInteger(
                                              nameIn(this, lsBlockName1 + ".processing_Cycle")),
                                          null,
                                          toChar(
                                              nameIn(this, lsBlockName1 + ".create_Dcr_Number")))),
                                      "no")) {
                                if (Objects.equals(
                                    toChar(
                                        refreshMasterLibrary.checkReferenceInfo(lsBlockName1, "D")),
                                    "N")) {
                                  throw new FormTriggerFailureException(event);

                                }
                              }
                            }
                          }

                          if (!Objects.equals(lnDcrNo, null)) {

                            alertDetails.getCurrent();
                            if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
                              vButton = moreButtons("S", "Reference Record",
                                  "This Fix has Reference.Do You Want to Delete the Fix?" + chr(10)
                                      + " ",
                                  "Yes", "No", "");
                              alertDetails.createNewRecord("refRec");
                              throw new AlertException(event, alertDetails);
                            } else {
                              vButton = alertDetails.getAlertValue("refRec",
                                  alertDetails.getCurrentAlert());
                            }
                            if (Objects.equals(vButton, 1)) {

                              // Map<String, Object> updRec = app.executeProcedure("CPTS",
                              // "Du_Update_Ref_Table_Prc", "Forms_Utilities",
                              // new ProcedureInParameter("pi_Old_dcrNumber",
                              // toInteger(nameIn(this, lsBlockName + ".create_Dcr_Number")),
                              // OracleTypes.NUMBER),
                              // new ProcedureInParameter("pi_New_dcrNumber", lnDcrNo,
                              // OracleTypes.NUMBER),
                              // new ProcedureInParameter("pi_Table_Name", lsTableName,
                              // OracleTypes.VARCHAR));

                              // coverity-fixes
                              app.executeProcedure("CPTS", "Du_Update_Ref_Table_Prc",
                                  "Forms_Utilities",
                                  new ProcedureInParameter("pi_Old_dcrNumber",
                                      toInteger(nameIn(this, lsBlockName + ".create_Dcr_Number")),
                                      OracleTypes.NUMBER),
                                  new ProcedureInParameter("pi_New_dcrNumber", lnDcrNo,
                                      OracleTypes.NUMBER),
                                  new ProcedureInParameter("pi_Table_Name", lsTableName,
                                      OracleTypes.VARCHAR));
                            } else {
                              throw new FormTriggerFailureException(event);

                            }

                          }

                          else if (Objects.equals(lsRefInfo, "N")) {
                            throw new FormTriggerFailureException(event);

                          }

                          if (Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {

                            refreshMasterLibrary.deleteFromRefTable(vDcrNumber, null);
                          }

                          if (Objects.equals(global.getLibRefreshed(), "Y")
                              && Arrays
                                  .asList(global.getNewProcessingCycle(),
                                      global.getOldProcessingCycle())
                                  .contains(toChar(vProcessingCycle))
                              && Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {

//								refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber, lnProcessingCycle,
//										pTableName, "I", null);
//								deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
                            if (parameter.getMultiRecDel() > 0) {
                              if (system.getCursorBlock().equals("PL_TLD_RUNWAY_MR")) {
                                chkSelectAll(isChecked, nameIn(this, system.getCursorBlock()));
                                for (PlTldRunwayMr plTldRunwayMr : plTldRunwayMr.getData()) {
                                  if (Objects.equals(plTldRunwayMr.getRecordStatus(), "DELETED")
                                      && Objects.equals(plTldRunwayMr.getChk(), "Y")) {
                                    pTableName = ltrim(upper(getBlockProperty("PL_TLD_RUNWAY_MR",
                                        "Query_Data_Source_Name")), "PL_");
                                    vDcrNumber = toInteger(plTldRunwayMr.getCreateDcrNumber());
                                    vProcessingCycle = toInteger(
                                        plTldRunwayMr.getProcessingCycle());
                                    refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber,
                                        lnProcessingCycle, pTableName, "I", null);
                                  }
                                }
                                validateCommit(reqDto);
                                commitForm(this);
                                sendUpdatedRowIdDetails();
                                system.setFormStatus("NORMAL");
                                coreptLib.dspMsg(
                                    "The refresh master library table for this deletion is done \n and all changes are commited.");
                                filterNonInsertedRecords(nameIn(this, system.getCursorBlock()));
                              }
                              controlBlock.setChkUnchkAll("N");
                              parameter.setMultiRecDel(0);
                            } else {
                              refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber,
                                  lnProcessingCycle, pTableName, "I", null);
                              copy("DELETED", lsBlockName + ".record_status");

                              validateCommit(reqDto);
                              commitForm(this);
                              sendUpdatedRowIdDetails();
                              refreshMasterLibrary.setRecordGroup(vDcrNumber, "I", lsBlockName1,
                                  vProcessingCycle, "D");
                              system.setRecordStatus("DELETED");
                              system.setFormStatus("NORMAL");
                              coreptLib.dspMsg(
                                  "The refresh master library table for this deletion is done \n and all changes are commited.");
                            }
                          } else if (parameter.getMultiRecDel() > 0) {
                            if (system.getCursorBlock().equals("PL_TLD_RUNWAY_MR")) {
                              chkSelectAll(isChecked, nameIn(this, system.getCursorBlock()));
                              validateCommit(reqDto);
                              commitForm(this);
                              sendUpdatedRowIdDetails();
                              system.setFormStatus("QUERIED");
                              filterNonInsertedRecords(nameIn(this, system.getCursorBlock()));
                            }
                            controlBlock.setChkUnchkAll("N");
                            parameter.setMultiRecDel(0);
                            coreptLib.dspMsg(
                                "The refresh master library table for this deletion is done \n and all changes are commited.");
                          } else {

                            //deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
                            copy("DELETED", lsBlockName + ".record_status");
                            String _rowId = toString(nameIn(this, lsBlockName + ".rowid"));
                            sendLockRowIdDetails(_rowId);
                            system.setRecordStatus("DELETED");
                            system.setFormStatus("CHANGED");
                          }
                        } else {
							throw new FormTriggerFailureException();
						}
					}
					} else {
						if (Objects.equals(parameter.getRecordType(), "T")) {

							coreptLib.dspActionMsg("D", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcr_Number")),
									toInteger(nvl(toChar(nameIn(this, lsBlockName + ".processing_Cycle")),
											global.getProcessingCycle())),
									toChar(nameIn(this, system.getCursorBlock() + ".customer_Ident")));

						} else {

							coreptLib.dspActionMsg("D", parameter.getRecordType(),
									toInteger(nameIn(this, "global.dcrNumber")),
									toInteger(nvl(toChar(nameIn(this, lsBlockName + ".processing_Cycle")),
											global.getProcessingCycle())),
									null);

						}
					}
				} else {
					if (Arrays.asList("Y", "S", "H", "O")
							.contains(toChar(nameIn(this, system.getCursorBlock() + ".validate_Ind")))) {

						refreshMasterLibrary.deleteFromRefTable(
								toInteger(nameIn(this, system.getCursorBlock() + ".create_Dcr_Number")), null);
					}

					//deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
					copy("DELETED", lsBlockName + ".record_status");
					deleteRecord(system.getCursorBlock());
					system.setRecordStatus("DELETED");
					system.setFormStatus("CHANGED");
				}
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyDelrec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyDelrec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenFormNavigate(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenFormNavigate Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getFormStatus(), "CHANGED")
					&& Objects.equals(toChar(nameIn(this, "global.check_save")), "Y")) {

				coreptLib.checkSavePrc("Runway");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenFormNavigate executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenFormNavigate Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void PlStdRunwayMrPreQuery() throws Exception {
		log.info("PlStdRunwayMrPreQuery Executing");
		try {
			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				plStdRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plStdRunwayMr.getRow(system.getCursorRecordIndex())
							.setProcessingCycle(global.getProcessingCycle());
				}
			}
			log.info("PlStdRunwayMrPreQuery Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing PlStdRunwayMrPreQuery" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrWhenClearBlock(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrWhenClearBlock Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			setItemProperty("control_block.std_validation_errors_mr", FormConstant.VISIBLE,
					FormConstant.PROPERTY_FALSE);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrWhenClearBlock executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrWhenClearBlock Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrProcessingCycleWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrProcessingCycleWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(rtrim(toChar(plStdRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle())),
					null)) {
				plStdRunwayMr.getRow(system.getCursorRecordIndex())
						.setProcessingCycle(global.getProcessingCycle());

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrProcessingCycleWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrProcessingCycleWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("plStdMrCntd");
			goItem("pl_std_runway_mr.airport_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plStdRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlStdRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("plStdMrCntd");
			goItem("pl_std_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlStdRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlStdRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void PlTldRunwayMrPreQuery() throws Exception {
		log.info("PlTldRunwayMrPreQuery Executing");
		try {
			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				plTldRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plTldRunwayMr.getRow(system.getCursorRecordIndex())
							.setProcessingCycle(global.getProcessingCycle());
				}

			}
		} catch (Exception e) {
			log.error("Error while executing PlTldRunwayMrPreQuery" + e.getMessage());
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrWhenClearBlock(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrWhenClearBlock Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			setItemProperty("control_block.tld_validation_errors_mr", FormConstant.VISIBLE,
					FormConstant.PROPERTY_FALSE);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrWhenClearBlock executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrWhenClearBlock Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrPostQuery(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" PlTldRunwayMrPostQuery Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			for (PlTldRunwayMr PlTldRunwayMr : plTldRunwayMr.getData()) {
				PlTldRunwayMr.setOldProcessingCycle(toInteger(PlTldRunwayMr.getProcessingCycle()));
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrPostQuery executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrCustomerIdentWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrCustomerIdentWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer vFlag = 0;
			Integer vAllowUpdate = 0;
			String vBlockName = system.getCursorBlock();

			// vAllowUpdate = 0;
			if (Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(global.getProcessingCycle()), global.getDataSupplier(),
								parameter.getRecordType(), toChar(nameIn(this, vBlockName + ".customer_ident")));

					} else {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(nameIn(this, vBlockName + ".processing_cycle")), global.getDataSupplier(),
								parameter.getRecordType(), toChar(nameIn(this, vBlockName + ".customer_ident")));

					}

				} else {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(global.getProcessingCycle()), global.getDataSupplier(),
								parameter.getRecordType(), null);

					} else {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(nameIn(this, vBlockName + ".processing_cycle")), global.getDataSupplier(),
								parameter.getRecordType(), null);

					}

				}
				if (Arrays.asList("J", "L", "E").contains(global.getDataSupplier())) {
					if (Arrays.asList(6, 4, 3, 2, 1).contains(vFlag)) {
						vAllowUpdate = 1;

					} else if (Objects.equals(vFlag, 0)) {
						if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {

							coreptLib.dspMsg("Customer is not associated with DCR# " + global.getDcrNumber()
									+ " Or with \n processing cycle " + global.getProcessingCycle());

						} else {

							coreptLib.dspMsg("Customer is not associated with DCR# " + global.getDcrNumber()
									+ " Or with \n processing cycle "
									+ toChar(nameIn(this, vBlockName + ".processing_cycle")));

						}
						throw new FormTriggerFailureException(event);
					}

				}

				else if (Arrays.asList("Q", "N").contains(global.getDataSupplier())) {
					if (Objects.equals(vFlag, 5)) {
						vAllowUpdate = 1;

					}
				}
				log.info("vall" + vAllowUpdate);
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrCustomerIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrCustomerIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrProcessingCycleWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldRunwayMrProcessingCycleWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle(), null)) {
				plTldRunwayMr.getRow(system.getCursorRecordIndex())
						.setProcessingCycle(global.getProcessingCycle());

			}

			if (!Objects.equals(system.getRecordStatus(), "NEW")) {
				if (!Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle(),
					toString(	plTldRunwayMr.getRow(system.getCursorRecordIndex()).getOldProcessingCycle()))) {
					if (Objects.equals(
							toChar(checkRunwayRef("pl_tld_runway",
									plTldRunwayMr.getRow(system.getCursorRecordIndex()).getRunwayIdent(),
									toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()),
									plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCustomerIdent(),
									toChar(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()))),
							"no")) {
						if (Objects.equals(toChar(refreshMasterLibrary.checkReferenceInfo("PL_TLD_RUNWAY_MR", "P")),
								"N")) {

							// TODO pause;
							plTldRunwayMr.getRow(system.getCursorRecordIndex()).setProcessingCycle(
									toString(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getOldProcessingCycle()));

						}

					}

				}

			}
			plTldRunwayMr.getRow(system.getCursorRecordIndex())
					.setOldProcessingCycle(toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getProcessingCycle()));

			OracleHelpers.ResponseMapper(this, reqDto);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrProcessingCycleWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrProcessingCycleWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrGeneratedInHouseFlagWhenValidateItem(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrGeneratedInHouseFlagWhenValidateItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getGeneratedInHouseFlag(),
						null)) {
					plTldRunwayMr.getRow(system.getCursorRecordIndex()).setGeneratedInHouseFlag("Y");

				}
			}

			OracleHelpers.ResponseMapper(this, reqDto);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrGeneratedInHouseFlagWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrGeneratedInHouseFlagWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("plTldMrCntd");
			goItem("pl_tld_runway_mr.customer_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("plTldMrCntd");
			goItem("pl_tld_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> plTldRunwayMrChkWhenCheckboxChanged(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" PlTldRunwayMrChkWhenCheckboxChanged Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
				parameter.setMultiRecDel(parameter.getMultiRecDel() + 1);

			}

			else if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "N")) {
				parameter.setMultiRecDel(parameter.getMultiRecDel() - 1);

			}

			String lsGroup = "NEW_DCR";
			RecordGroup groupId = findGroup(lsGroup);
			Integer lnRow = getGroupRowCount(groupId);
			// TODO Object colId = findColumn("NEW_DCR.DCR_NO");

			Number colVal = null;

			if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
				addGroupRow(groupId, "end_of_group");
				// lnRow = lnRow + 1;

				setGroupNumberCell(groupId, "dcrNo", lnRow,
						toInteger(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				// setgroup_number_cell("NEW_DCR.DCR_NO",ln_row,plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber());
				global.setNewDcrNo(
						rtrim(ltrim(
								rtrim(global.getNewDcrNo(), ", ") + ","
										+ plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber(),
								", "), ", "));

			} else if (Objects.equals(nvl(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "N"), "N")) {
				for (int i = 0; i < lnRow; i++) {
					// TODO colVal = getGroupNumberCell(colId,i);
					colVal = getGroupNumberCell("newDcr.dcrNo", i);
					if (Objects.equals(toInteger(  colVal),
						toInteger(	plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()))) {
						deleteGroupRow("newDcr", i);
						break;
					}
				}

				// TODO delete_dcr_no --- Program Unit Calling
				deleteDcrNo();
			}
			global.setNewDcrNo(ltrim(global.getNewDcrNo(), ","));
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" PlTldRunwayMrChkWhenCheckboxChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the PlTldRunwayMrChkWhenCheckboxChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void stdRunwayMrPreQuery() throws Exception {
		log.info("stdRunwayMrPreQuery Executing");
		try {
			stdRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
			log.info("stdRunwayMrPreQuery Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing stdRunwayMrPreQuery" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> stdRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("stdMrCntd");
			goItem("std_runway_mr.airport_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> stdRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" stdRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("stdMrCntd");
			goItem("std_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void tldRunwayMrPreQuery() throws Exception {
		log.info("tldRunwayMrPreQuery Executing");
		try {
			tldRunwayMr.getRow(system.getCursorRecordIndex()).setDataSupplier(global.getDataSupplier());
			log.info("tldRunwayMrPreQuery Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing tldRunwayMrPreQuery" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> tldRunwayMrCloseDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" tldRunwayMrCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("tldMrCntd");
			goItem("tld_runway_mr.customer_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldRunwayMrCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldRunwayMrCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> tldRunwayMrOpenDetailsButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" tldRunwayMrOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("tldMrCntd");
			goItem("tld_runway_mr.second_localizer_mls_gls_ident");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldRunwayMrOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldRunwayMrOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockTldOverideMrWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockTldOverideMrWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vItem = system.getCursorItem();

			parameter.setTldOverrideButtonFlg("MR");
			showView("tldOver");
			goItem("control_block.tld_override_errors");
			goItem(vItem);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockTldOverideMrWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockTldOverideMrWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockStdOverideMrWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockStdOverideMrWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vItem = system.getCursorItem();

			parameter.setStdOverrideButtonFlg("MR");
			showView("stdOver");
			goItem("control_block.std_override_errors");
			goItem(vItem);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockStdOverideMrWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockStdOverideMrWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockCloseTldOverrideWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockCloseTldOverrideWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("tldOver");
			if (Objects.equals(parameter.getTldOverrideButtonFlg(), "MR")) {
				goItem("pl_tld_runway_mr.customer_ident");

			}

			else if (Objects.equals(parameter.getTldOverrideButtonFlg(), "SR")) {
				goItem("pl_tld_runway_sr.customer_ident");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockCloseTldOverrideWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockCloseTldOverrideWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockCloseStdOverrideWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockCloseStdOverrideWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("stdOver");
			if (Objects.equals(parameter.getStdOverrideButtonFlg(), "MR")) {
				goItem("pl_std_runway_mr.airport_ident");

			}

			else if (Objects.equals(parameter.getStdOverrideButtonFlg(), "SR")) {
				goItem("pl_std_runway_sr.airport_ident");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockCloseStdOverrideWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockCloseStdOverrideWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> controlBlockChkUnchkAllWhenCheckboxChanged(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockChkUnchkAllWhenCheckboxChanged Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			RecordGroup groupId = findGroup("newDcr");
			Integer lnRow = getGroupRowCount(groupId);
			Number colVal = null;
			// Integer lnRow1 = 0;

			if (Objects.equals(controlBlock.getChkUnchkAll(), "Y")) {
				if (findGroup("newDcr") != null) {
					deleteGroupRow("newDcr", "ALL_ROWS");

				}

				for (int i = 1; i <= lnRow; i++) {

					// TODO delete_dcr_no --- Program Unit Calling
					deleteDcrNo();

				}
				lnRow = getGroupRowCount(groupId);
				goBlock("PlTldRunwayMr", "");

				// TODO First_record;
				for (PlTldRunwayMr PlTldRunwayMr : plTldRunwayMr.getData()) {

					PlTldRunwayMr.setChk("Y");
					parameter.setMultiRecDel(parameter.getMultiRecDel() + 1);
					addGroupRow(groupId, "end_of_group");
					//

					// set_group_number_cell("NEW_DCR.DCR_NO",ln_row,PlTldRunwayMr.getCreateDcrNumber());
					setGroupNumberCell(groupId, "dcrNo", lnRow,
							toInteger(PlTldRunwayMr.getCreateDcrNumber()));
					global.setNewDcrNo(rtrim(ltrim(
							global.getNewDcrNo() + "," + PlTldRunwayMr.getCreateDcrNumber(), ", "),
							", "));
					lnRow = lnRow + 1;
					if (!Objects.equals(PlTldRunwayMr.getCustomerIdent(), null)) {
						// nextRecord("");

					} else {
						// clearRecord("");
						PlTldRunwayMr.setChk("Y");
						break;
					}
				}
				// TODO First_record;
			} else {
				parameter.setMultiRecDel(0);
				lnRow = getGroupRowCount(groupId) - 1;
				goBlock("PlTldRunwayMr", "");

				// TODO First_record;
				system.setCursorRecordIndex(0);
				for (int i = 1; i <= plTldRunwayMr.size(); i++) {

					if (Objects.equals(plTldRunwayMr.getRow(system.getCursorRecordIndex()).getChk(), "Y")) {
						plTldRunwayMr.getRow(system.getCursorRecordIndex()).setChk("N");
						for (int x = 0; x < lnRow; x++) {
							colVal = getGroupNumberCell("newDcr.dcrNo", x);
							if (Objects.equals(toInteger( colVal),
								toInteger(	plTldRunwayMr.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())) ){
								deleteGroupRow("newDcr", x);
								break;
							}
						}
					}
					// TODO delete_dcr_no --- Program Unit Calling
					deleteDcrNo();
					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
				}
				// TODO First_record;
			}
			global.setNewDcrNo(ltrim(global.getNewDcrNo(), ","));
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockChkUnchkAllWhenCheckboxChanged executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockChkUnchkAllWhenCheckboxChanged Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyCommit(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCommit Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (global.getClearBlock()) {
				validateCommit(reqDto);
				try {
					commitForm(this);
					sendUpdatedRowIdDetails();
					coreptLib.message("Record has been saved successfully");
				} catch (Exception e) {
					if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
							|| e instanceof EntityExistsException
							|| e.getCause() instanceof ConstraintViolationException
							|| e.getMessage().contains("ORA-00001")) {
						global.setErrorCode(501);

						coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
								+ "error message from the \"Display \n Error\" in the \"HELP\" menu");

						log.info(" Unique Constrain Error while Executing the keyCommit Service");
					} else {
						throw e;
					}
				}
				global.setClearBlock(false);
			} else {
				if (Objects.equals(parameter.getWorkType(), "VIEW")) {
					// null
				} else {

					checkToCommit("COMMIT", reqDto);
					system.setMode("NORMAL");
					system.setRecordStatus("QUERIED");
				}
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

	@Transactional
	public void checkToCommit(String pActionType, RunwayTriggerRequestDto reqDto) throws Exception {
		log.info("checkToCommit Executing");
		OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		// String query = "";
		// Record rec = null;
		try {
			Integer vButton = 0;
			Integer totalRows = 0;
			// String vTemp = null;
			String vButtonText = null;
			// Object msgnum = messacode;

			if (Objects.equals(system.getFormStatus(), "CHANGED")) {
				if (Objects.equals(pActionType, "COMMIT")) {
					vButtonText = "Cancel";

				}

				else if (Objects.equals(pActionType, "EXIT")) {
					vButtonText = "Exit Without Save";

				}

				else {
					vButtonText = "Cancel Modification";

				}
				if (Objects.equals(global.getLibRefreshed(), "Y")) {
					OracleHelpers.bulkClassMapper(this, displayAlert);
					alertDetails.getCurrent();
					if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
						vButton = displayAlert.moreButtons("S", "Refresh Record",
								"You have modified record(s). Select an option:" + chr(10) + chr(10)
										+ "1. Save and refresh Master Library" + chr(10)
										+ "2. Cancel modification, NO Save, NO Refresh",
								"Save&Refresh", vButtonText, "");
						OracleHelpers.bulkClassMapper(displayAlert, this);
						alertDetails.createNewRecord("alertid");
						throw new AlertException(event, alertDetails);
					} else {
						vButton = alertDetails.getAlertValue("alertid", alertDetails.getCurrentAlert());
					}
					if (Arrays.asList(1).contains(vButton)) {

						validateCommit(reqDto);
						try {
							commitForm(this);
							sendUpdatedRowIdDetails();
							coreptLib.message("Record has been saved successfully");
							system.setFormStatus("NORMAL");
							totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
							if (totalRows > 0) {

								OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
								refreshMasterLibrary.refreshRecords(totalRows);

							}

						} catch (Exception e) {
							if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
									|| e instanceof EntityExistsException
									|| e.getCause() instanceof ConstraintViolationException
									|| e.getMessage().contains("ORA-00001")) {
								global.setErrorCode(501);

								coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
										+ "error message from the \"Display \n Error\" in the \"HELP\" menu");

								log.info(" Unique Constrain Error while Executing the keyCommit Service");
							} else {
								throw e;
							}
						}
//						totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
//						if (totalRows > 0) {
//
//							OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
//							refreshMasterLibrary.refreshRecords(totalRows);
//
//						}

						// if ((Objects.equals(msgnum, 40400))) {
						//
						// // TODO CLEAR_MESSAGE;
						// clearMessage();
						//
						// }

					}

				}

				else {
					if (Objects.equals(pActionType, "COMMIT")) {
						vButton = 1;

					}

					else {
						OracleHelpers.bulkClassMapper(this, displayAlert);
						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
							vButton = displayAlert.moreButtons("S", "Refresh Record",
									"Record is modified or inserted or deleted. Select an option: " + "\n ",
									"Save", vButtonText, " ");
							OracleHelpers.bulkClassMapper(displayAlert, this);
							alertDetails.createNewRecord("alertid");
							throw new AlertException(event, alertDetails);
						} else {
							vButton = alertDetails.getAlertValue("alertid", alertDetails.getCurrentAlert());
						}

					}
					if (Objects.equals(vButton, 1)) {

						validateCommit(reqDto);
						try {
							commitForm(this);
							coreptLib.message("Record has been saved successfully");
							system.setFormStatus("NORMAL");
						} catch (Exception e) {
							if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
									|| e instanceof EntityExistsException
									|| e.getCause() instanceof ConstraintViolationException
									|| e.getMessage().contains("ORA-00001")) {
								global.setErrorCode(501);

								coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
										+ "error message from the \"Display \n Error\" in the \"HELP\" menu");

								log.info(" Unique Constrain Error while Executing the keyCommit Service");
							} else {
								throw e;
							}
						}

						// if ((Objects.equals(msgnum, 40400))) {
						//
						// // TODO CLEAR_MESSAGE;
						// clearMessage();
						//
						// }

					}

					else {

						// TODO EXIT_FORM(no_commit);
						exitForm();

					}

				}
				if ((Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 2))
						|| (!Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 1))) {

//					if (Objects.equals(pActionType, "COMMIT")) {
//					}
//
//					else if (Objects.equals(pActionType, "EXIT")) {
//
//						setApplicationProperty("cursorStyle", "default");
//						exitForm();
//
//					}
//
//					else {
//						clearBlock("noCommit", "");
//					}
					if (Objects.equals(pActionType, "COMMIT"))
						;
		        	else if (Objects.equals(pActionType, "EXIT")) {
		            exitForm();
		          }
		        	else {
		        		system.setFormStatus("NORMAL");
		        	}

				}

				else {
					 if (Objects.equals(system.getFormStatus(), "CHANGED")) {
					 throw new FormTriggerFailureException();
					 }else if (Objects.equals(pActionType, "EXIT")) {
							setApplicationProperty("cursorStyle", "default");
							exitForm();
					}

				}

			}

			else {

				OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
				refreshMasterLibrary.ifRefresh();
				if(pActionType.equals("EXIT")) {
					exitForm();
				}
			}

			log.info("checkToCommit Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing checkToCommit" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyEdit(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyEdit Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptTemplate);
			coreptTemplate.keyEdit();
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyDuprec(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDuprec Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			}

			else {
				if (!Objects.equals(nameIn(this, system.getCursorBlock() + ".data_supplier"), null)) {
					createRecord(system.getCurrentBlock());

				}

				duplicateRecord(system.getCurrentBlock(), system.getCursorRecordIndex() + 1);
				copy(this, null, system.getCursorBlock() + ".validate_ind");
				copy(this, null, system.getCursorBlock() + ".create_dcr_number");
				copy(this, null, system.getCursorBlock() + ".update_dcr_number");
				copy(this, null, system.getCursorBlock() + ".processing_cycle");
				if (nameIn(this, system.getCursorBlock() + ".cycle_Data") != null) {
					// null;
				}

				else {
					copy(this, null, system.getCursorBlock() + ".cycle_Data");

				}
				if (Objects.equals(parameter.getRecordType(), "T")) {
					copy(this, "Y", system.getCursorBlock() + ".generated_in_house_flag");
				}

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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyNxtblk(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyNxtblk Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptLib);
			coreptLib.activateRole();
			// null;
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyNxtblk executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyNxtblk Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyPrvblk(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyPrvblk Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// null;
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyPrvblk executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyPrvblk Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyDupItem(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDupItem Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			} else {

				// TODO duplicate_item;
				duplicateItem(system.getCurrentItem());
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyCrerec(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCrerec Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			} else {
				createRecord(system.getCurrentBlock());

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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyClrfrm(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyClrfrm Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// null
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyClrfrm Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenNewBlockInstance(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenNewBlockInstance Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")
					&& like("PL_%", system.getCursorBlock())) {

				OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
				refreshMasterLibrary.setKeyUpdateFalse(HoneyWellUtils.toCamelCase(system.getCursorBlock()));

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> whenValidateRecord(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			String pBlock = system.getCursorBlock();

			if (Objects.equals(system.getCursorBlock(), "PL_STD_RUNWAY_MR")) {
				plStdRunwayMrProcessingCycleWhenValidateItem(reqDto);
			} else if (Objects.equals(system.getCursorBlock(), "PL_TLD_RUNWAY_MR")) {

				plTldRunwayMrCustomerIdentWhenValidateItem(reqDto);
				plTldRunwayMrProcessingCycleWhenValidateItem(reqDto);
				plTldRunwayMrGeneratedInHouseFlagWhenValidateItem(reqDto);

			}

			if (like("PL_%", pBlock) && Arrays.asList("CHANGED", "INSERT").contains(system.getRecordStatus())) {
				if (!Arrays.asList("Y", "S", "H", "W", "N", "I", "O").contains(nvl(toChar(
						nameIn(this, pBlock + ".validate_Ind")),
						"Y"))) {

					coreptLib.dspMsg("Validate indicator can only 'Y','S','H','W','N' or 'I'");
					throw new FormTriggerFailureException(event);

				}

				if (Objects.equals(parameter.getRecordType(), "T")) {
					coreptLib.validateextrafields(
							toInteger(nameIn(this, pBlock + ".processing_Cycle")),
							!Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "")
									? toChar(nameIn(this, pBlock + ".generated_In_House_Flag"))
									: null);
				}

				if (!Objects.equals(
						nameIn(this, pBlock + ".processing_Cycle"),
						null)
						&& Objects.equals(nameIn(this, pBlock + ".data_Supplier"), null)) {
					copy(this, global.getDataSupplier(), pBlock + ".data_Supplier");
				}

				// TODO do_validate(p_block) --- Program Unit Calling
				doValidate(pBlock, "Y");
				if (!Objects.equals(parameter.getWorkType(), "VIEW")
						&& Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(toChar(nameIn(this, pBlock + ".generated_In_House_Flag")), "N")) {
						setItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						if (!like("%MR", pBlock)) {
							setItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "notUpdatable");
							setItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute",
									"notUpdatable");
							setItemProperty(pBlock + ".validate_ind", "current_record_attribute", "default");

						}
					} else {
						setItemProperty(pBlock + ".processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemProperty(pBlock + ".generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemProperty(pBlock + ".validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						if (!like("%MR", pBlock)) {
							setItemProperty(pBlock + ".processing_cycle", "current_record_attribute", "default");
							setItemProperty(pBlock + ".generated_in_house_flag", "current_record_attribute", "default");
							setItemProperty(pBlock + ".validate_ind", "current_record_attribute", "notUpdatable");
						}
					}
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenValidateRecord Service");
			if(Objects.equals(reqDto, null)) {
				throw e;
			}
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> keyExit(RunwayTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExit Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
//			OracleHelpers.bulkClassMapper(this, coreptTemplate);
//			coreptTemplate.keyExit();
			if (Objects.equals(system.getMode(), "NORMAL") && !Objects.equals(parameter.getWorkType(), "VIEW")) {

				checkToCommit("EXIT",reqDto);

			} else {

				setApplicationProperty("cursorStyle", "default");
				exitForm();
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

	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// displayItemBlockFormPartNumberWhenNewItemInstance(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" displayItemBlockFormPartNumberWhenNewItemInstance Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" displayItemBlockFormPartNumberWhenNewItemInstance executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// displayItemBlockFormPartNumberWhenNewItemInstance Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> displayItemBlockRefreshButtonWhenButtonPressed(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" displayItemBlockRefreshButtonWhenButtonPressed Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer totalRows = null;
			if (Objects.equals(system.getFormStatus(), "CHANGED")) {
				coreptLib.dspMsg("There is changes in the form, please do commit first.");
			} else {
				Integer vButton = null;

				totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
				alertDetails.getCurrent();
				if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
					vButton = moreButtons("S", "Refresh Record",
							"You have modified or inserted or deleted " + totalRows + " record(s). " + chr(10)
									+ "Do you want to refresh the Master Library now?" + chr(10) + chr(10),
							"Refresh", "Cancel", "");
					alertDetails.createNewRecord("refRec");
					throw new AlertException(event, alertDetails);
				} else {
					vButton = alertDetails.getAlertValue("refRec", alertDetails.getCurrentAlert());
				}
				if (Objects.equals(vButton, 1)) {
					refreshMasterLibrary.refreshRecords(totalRows);
				} else {
					// null;
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void validateCommit(RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" validateCommit Executing");
		try {
			Object obj = OracleHelpers.findBlock(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()));
			int i = 0, backupCursorRecordIndex = system.getCursorRecordIndex();
			if (obj instanceof Block<?>) {
				Block<?> blocks = (Block<?>) obj;
				for (Object block : blocks.getData()) {

					// system.setCursorRecordIndex(i);
					if (Objects.equals(nameIn(block, "record_Status"), "INSERT")) {
						system.setRecordStatus("INSERT"); // JJ
						system.setCursorRecordIndex(i); // JJ
						whenValidateRecord(reqDto); // JJ

						preInsert(reqDto);
					} else if (Objects.equals(nameIn(block, "record_Status"), "CHANGED")) {
						system.setRecordStatus("CHANGED"); // J
						system.setCursorRecordIndex(i); // J
						whenValidateRecord(reqDto); // J
						preUpdate(reqDto);
					}
					i = i + 1;
				}
			}
			system.setCursorRecordIndex(backupCursorRecordIndex);
			whenValidateRecord(reqDto);
			
			log.info(" validateCommit executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the validateCommit Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> toolsDuplicate(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
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
	public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>> toolsExportDestination(
			RunwayTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
		RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
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
			if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("stdRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("stdRunwayMr");
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
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					StdRunwayMr stdRunwayMr = app.mapResultSetToClass(mstRec, StdRunwayMr.class);
					reportfile.append(getExportData(stdRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
				}
			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("tldRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("tldRunwayMr");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					TldRunwayMr tldRunwayMr = app.mapResultSetToClass(mstRec, TldRunwayMr.class);
					reportfile.append(getExportData(tldRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
				}

			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("plStdRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("plStdRunwayMr");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					PlStdRunwayMr PlStdRunwayMr = app.mapResultSetToClass(mstRec, PlStdRunwayMr.class);
					reportfile.append(getExportData(PlStdRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
				}
			} else if (HoneyWellUtils.toCamelCase(system.getCursorBlock()).equals("plTldRunwayMr")) {
				mstBlockData = reqDto.getExportDataBlocks().get("plTldRunwayMr");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				}
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					PlTldRunwayMr PlTldRunwayMr = app.mapResultSetToClass(mstRec, PlTldRunwayMr.class);
					reportfile.append(getExportData(PlTldRunwayMr, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));
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

	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilDummyWhenButtonPressed(RunwayTriggerRequestDto reqDto) throws
	// Exception{
	// log.info(" webutilDummyWhenButtonPressed Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilDummyWhenButtonPressed executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the webutilDummyWhenButtonPressed Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilClientinfoFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilClientinfoFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilFileFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilFileFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilHostFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilHostFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilSessionFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilSessionFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilFiletransferFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent
	// Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilFiletransferFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilOleFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto reqDto)
	// throws Exception{
	// log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilOleFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilCApiFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilCApiFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<RunwayTriggerResponseDto>>
	// webutilWebutilBrowserFunctionsWhenCustomItemEvent(RunwayTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<RunwayTriggerResponseDto> responseObj = new BaseResponse<>();
	// RunwayTriggerResponseDto resDto = new RunwayTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// webutilWebutilBrowserFunctionsWhenCustomItemEvent Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// public void updateAppInstance() {
	// app.getDb();
	// }
}


