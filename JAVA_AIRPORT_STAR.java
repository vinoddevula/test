package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.AirportRgRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.FixRgRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.response.AirportRgResponseDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.response.FixRgResponseDto;
import com.honeywell.coreptdu.datatypes.airportstar.service.IAirportStarLovService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AirportStarLovServiceImpl implements IAirportStarLovService {
	@Autowired
	private IApplication app;

	@Override
	public ResponseEntity<ResponseDto<List<FixRgResponseDto>>> fixRg(Integer page, Integer rec,
			FixRgRequestDto reqDto) {
		BaseResponse<List<FixRgResponseDto>> responseObj = new BaseResponse<>();
		List<FixRgResponseDto> resList = new ArrayList<>();
		try {
			String lovQuery = """
								select airport_ident,fix_ident Ident,fix_icao ICAO,record_type Type,processing_cycle Proc_Cycle,fix_section S,fix_subsection SS,Cust_Ident Cust,latitude,longitude
					from  all_fixes_vw_syn
					where  fix_ident = ?
					and (? IS NULL or fix_icao = ?)
					and  processing_cycle in  (util2.get_previous_cycle(?),? )
					and    data_supplier    = ?
					and    validate_ind in ('Y','O')
					and   ( record_type = 'S'
					OR ( record_type = 'T' AND cust_ident = ? and    generated_in_house_flag = 'Y'))
					order by Proc_Cycle desc,type,airport_ident
								""";
			List<Record> lovRecs = app.executeQuery(lovQuery, reqDto.getPlTldStarLegFixIdent(),
					reqDto.getParameterFixIcao(), reqDto.getParameterFixIcao(), reqDto.getPlTldStarProcessingCycle(),
					reqDto.getPlTldStarProcessingCycle(), reqDto.getGlobalDataSupplier(),
					reqDto.getPlTldStarCustomerIdent());
			for (Record lovRec : lovRecs) {
				FixRgResponseDto fixrgDto = new FixRgResponseDto();
				fixrgDto.setAirportIdent(lovRec.getString());
				fixrgDto.setIdent(lovRec.getString());
				fixrgDto.setIcao(lovRec.getString());
				fixrgDto.setType(lovRec.getString());
				fixrgDto.setProcCycle(lovRec.getString());
				fixrgDto.setS(lovRec.getString());
				fixrgDto.setSs(lovRec.getString());
				fixrgDto.setCust(lovRec.getString());
				fixrgDto.setLatitude(lovRec.getString());
				fixrgDto.setLongitude(lovRec.getString());
				resList.add(fixrgDto);
			}
			log.info("fixRg Lov Executed Successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resList));
		} catch (Exception e) {
			log.info("Error while Executing fixRg Lov");
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<AirportRgResponseDto>>> airportRg(Integer page, Integer rec,
			AirportRgRequestDto reqDto) {
		BaseResponse<List<AirportRgResponseDto>> responseObj = new BaseResponse<>();
		List<AirportRgResponseDto> resList = new ArrayList<>();
		try {
			String lovQuery = """
								SELECT 'STD' TYPE,
					       airport_ident,
					       airport_icao,
					       airport_name
					  FROM pl_std_airport
					 WHERE airport_ident LIKE
					          RTRIM (?, '%') || '%'
					       AND processing_cycle = ?
					       AND data_supplier = ?
					       AND validate_ind IN ('Y', 'S', 'H', 'O')
					UNION
					SELECT 'TLD' TYPE,
					       airport_ident,
					       airport_icao,
					       airport_name
					  FROM pl_tld_airport
					 WHERE airport_ident LIKE
					          RTRIM (?, '%') || '%'
					       AND processing_cycle = ?
					       AND data_supplier = ?
					       AND validate_ind IN ('Y', 'S', 'H', 'O')
					       AND CUSTOMER_IDENT = ?
					       AND generated_in_house_flag LIKE
					              RTRIM (?, 'Y') || '%'
					UNION
					SELECT 'TLD' TYPE,
					       airport_ident,
					       airport_icao,
					       airport_name
					  FROM pl_tld_airport
					 WHERE airport_ident LIKE
					          RTRIM (?, '%') || '%'
					       AND processing_cycle = ?
					       AND data_supplier = ?
					       AND validate_ind IN ('Y', 'S', 'H', 'O')
					       AND CUSTOMER_IDENT = ?
					       AND generated_in_house_flag LIKE
					              RTRIM (?, 'Y') || '%'
					ORDER BY TYPE, airport_ident
								""";
			List<Record> lovRecs = app.executeQuery(lovQuery, reqDto.getPlTldStarAirportIdent(),
					reqDto.getParameterCompCycle(), reqDto.getGlobalDataSupplier(), reqDto.getPlTldStarAirportIdent(),
					reqDto.getPlTldStarProcessingCycle(), reqDto.getGlobalDataSupplier(),
					reqDto.getPlTldStarCustomerIdent(), reqDto.getPlTldStarGeneratedInHouseFlag(),
					reqDto.getPlTldStarAirportIdent(), reqDto.getParameterCompCycle(), reqDto.getGlobalDataSupplier(),
					reqDto.getPlTldStarCustomerIdent(), reqDto.getPlTldStarGeneratedInHouseFlag());
			for (Record lovRec : lovRecs) {
				AirportRgResponseDto airportrgDto = new AirportRgResponseDto();
				airportrgDto.setType(lovRec.getString());
				airportrgDto.setAirportIdent(lovRec.getString());
				airportrgDto.setAirportIcao(lovRec.getString());
				airportrgDto.setAirportName(lovRec.getString());
				resList.add(airportrgDto);
			}
			log.info("airportRg Lov Executed Successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resList));
		} catch (Exception e) {
			log.info("Error while Executing airportRg Lov");
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}
----------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Connection;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.airportsid.entity.PlStdSidLeg;
import com.honeywell.coreptdu.datatypes.airportsid.entity.PlTldSidLeg;
import com.honeywell.coreptdu.datatypes.airportsid.entity.TldSidLeg;
import com.honeywell.coreptdu.datatypes.airportstar.block.ControlBlock;
import com.honeywell.coreptdu.datatypes.airportstar.block.Webutil;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.AirportStarTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarLegQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarSegmentQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarLegQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarSegmentQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.response.AirportStarTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStar;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStar;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStar;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStar;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.service.IAirportStarTriggerService;
import com.honeywell.coreptdu.datatypes.airportwaypoint.dto.request.AirportWaypointTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.coreptmenummb.serviceimpl.CoreptMenuMmbServiceImpl;
import com.honeywell.coreptdu.datatypes.corepttemplate.block.DisplayItemBlock;
import com.honeywell.coreptdu.datatypes.corepttemplate.serviceimpl.CoreptTemplateTriggerServiceImpl;
import com.honeywell.coreptdu.datatypes.exportdestination.block.SelectOptions;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.request.ExportDestinationTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.exportdestination.dto.response.ExportDestinationTriggerResponseDto;
import com.honeywell.coreptdu.exception.AlertException;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.exception.ExitFormException;
import com.honeywell.coreptdu.exception.FormTriggerFailureException;
import com.honeywell.coreptdu.exception.NoDataFoundException;
import com.honeywell.coreptdu.exception.TooManyRowsException;
import com.honeywell.coreptdu.global.dbtype.CrAirportProcedure;
import com.honeywell.coreptdu.global.dbtype.LegRec;
import com.honeywell.coreptdu.global.dbtype.RecProcRdv;
import com.honeywell.coreptdu.global.dbtype.SegRec;
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
import com.honeywell.coreptdu.global.forms.FormConstant;
import com.honeywell.coreptdu.global.forms.WindowDetail;
import com.honeywell.coreptdu.pkg.spec.IDisplayAlert;
import com.honeywell.coreptdu.pkg.spec.IRefreshMasterLibrary;
import com.honeywell.coreptdu.pll.CoreptLib;
import com.honeywell.coreptdu.pll.CoreptLib.fixValuePrcRes;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.common.CustomInteger;
import com.honeywell.coreptdu.utils.common.HashUtils;
import com.honeywell.coreptdu.utils.dbutils.GenericNativeQueryHelper;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInOutParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureInParameter;
import com.honeywell.coreptdu.utils.dbutils.ProcedureOutParameter;
import com.honeywell.coreptdu.utils.oracleutils.Block;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.HoneyWellUtils;
import com.honeywell.coreptdu.utils.oracleutils.OracleArray;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.PropertyHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroup;

import jakarta.persistence.EntityExistsException;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.internal.OracleTypes;

@Slf4j
@Service
@RequestScope
public class AirportStarTriggerServiceImpl extends GenericTemplateForm<AirportStarTriggerServiceImpl>
		implements IAirportStarTriggerService {

	@Getter
	@Setter
	private Block<TldStar> tldStar = new Block<>();
	@Getter
	@Setter
	private ControlBlock controlBlock = new ControlBlock();
	@Getter
	@Setter
	private Block<TldStarSegment> tldStarSegment = new Block<>();
	@Getter
	@Setter
	private Block<StdStarSegment> stdStarSegment = new Block<>();
	@Getter
	@Setter
	private Block<PlTldStarSegment> plTldStarSegment = new Block<>();
	@Getter
	@Setter
	private Block<TldStarLeg> tldStarLeg = new Block<>();
	@Getter
	@Setter
	private Block<StdStarLeg> stdStarLeg = new Block<>();
	@Getter
	@Setter
	private Webutil webutil = new Webutil();
	@Getter
	@Setter
	private Block<PlTldStarLeg> plTldStarLeg = new Block<>();
	@Getter
	@Setter
	private PlStdStar plStdStar = new PlStdStar();
	@Getter
	@Setter
	private Block<StdStar> stdStar = new Block<>();
	@Getter
	@Setter
	private Block<PlStdStarLeg> plStdStarLeg = new Block<>();
	@Getter
	@Setter
	private PlTldStar plTldStar = new PlTldStar();
	@Getter
	@Setter
	private Block<PlStdStarSegment> plStdStarSegment = new Block<>();
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
	private SelectOptions selectOptions = new SelectOptions();
	@Autowired
	private IApplication app;
	@Autowired
	private HashUtils hashUtils;
	@Autowired
	private CoreptLib coreptLib;
	@Autowired
	private CoreptTemplateTriggerServiceImpl coreptTemplateTriggerServiceImpl;
	@Autowired
	private CoreptMenuMmbServiceImpl coreptMenuMmbServiceImpl;
	@Autowired
	private GenericNativeQueryHelper genericNativeQueryHelper;
	@Autowired
	private IRefreshMasterLibrary refreshMasterLibrary;
	@Getter
	@Setter
	private Globals globals = new Globals();
	@Getter
	@Setter
	private AlertDetail alertDetails = new AlertDetail();
	@Getter
	@Setter
	private List<String> blocksOrder = new ArrayList<>();
	@Getter
	@Setter
	private Map<String, WindowDetail> windows = new HashMap<>();

	@Value("${web.ploturl}")
	private String webPlotUrl;

	// @Autowired
	// private ICheckProcs checkProcs;
	@Autowired
	protected IDisplayAlert displayAlert;

	static class vException extends Exception {
		public vException(String message) {
			super(message);
		}
	}

	//
	//
	//// TODO PUnits Manual configuration
	// // ParentName ---> SET_VALIDATE_IND
	//// File Name ---> corept_template.fmb
	//// TODO PUnits Manual configuration
	// // ParentName ---> UPDATE_PROC_TYPE_AND_ALTITUDE
	//// File Name ---> corept_template.fmb
	//// TODO PUnits Manual configuration
	// // ParentName ---> POPULATE_ITEMS
	//// File Name ---> corept_template.fmb
	//// TODO PUnits Manual configuration
	// // ParentName ---> INITIALIZE_FORM
	//// File Name ---> corept_template.fmb
	//// TODO PUnits Manual configuration
	// // ParentName ---> DSP_ERROR
	//// File Name ---> corept_template.fmb
	//
	// @Override
	// public void checkPackageFailure() throws Exception{
	// log.info("checkPackageFailure Executing");
	// String query = "";
	// Record rec = null;
	// try {
	//
	// if(!(formSuccess)) {
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	//
	// log.info("checkPackageFailure Executed Successfully");
	// }
	// catch(Exception e){
	// log.error("Error while executing checkPackageFailure"+e.getMessage());
	// throw e;
	//
	// }
	// }
	//
	//
	private void queryMasterDetails() throws Exception {
		log.info("queryMasterDetails Executing");
		String where = "";
		String oldmsg = null;
		try {
			String reldef = "FALSE";
			try {
				oldmsg = system.getMessageLevel();
				if (Objects.equals(reldef, "FALSE")) {

					system.setMessageLevel("10");
					where = "'" + plTldStar.getAirportIdent() + "' = airport_ident and '" + plTldStar.getAirportIcao()
							+ "' = airport_icao and '" + plTldStar.getStarIdent() + "' = star_ident and '"
							+ plTldStar.getDataSupplier() + "' = data_supplier and '" + plTldStar.getProcessingCycle()
							+ "' = processing_cycle and '" + plTldStar.getCustomerIdent() + "' = customer_ident ";
					executeQuery(this, "plTldStarLeg", app.sanitizeValueCheck(where), null, null);

					where = "'" + plTldStar.getAirportIdent() + "' = airport_ident and '" + plTldStar.getAirportIcao()
							+ "' = airport_icao and '" + plTldStar.getStarIdent() + "' = star_ident and '"
							+ plTldStar.getDataSupplier() + "' = data_supplier and '" + plTldStar.getProcessingCycle()
							+ "' = processing_cycle and '" + plTldStar.getCustomerIdent() + "' = customer_ident ";
					executeQuery(this, "plTldStarSegment", app.sanitizeValueCheck(where), null, null);

					system.setMessageLevel(oldmsg);
				}
			} catch (FormTriggerFailureException e) {
				system.setMessageLevel(oldmsg);
			}
			log.info("queryMasterDetails Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing queryMasterDetails" + e.getMessage());
			throw e;

		}
		// Coverity Fixes
		finally {
			system.setMessageLevel(oldmsg);
		}
	}

	//
	//
	// @Override
	// public void clearAllMasterDetails() throws Exception{
	// log.info("clearAllMasterDetails Executing");
	// String query = "";
	// Record rec = null;
	// try {
	// String mastblk = null;
	// String coordop = null;
	// String trigblk = null;
	// String startitm = null;
	// String frmstat = null;
	// String curblk = null;
	// String currel = null;
	// String curdtl = null;
	/// **
	// FUNCTION First_Changed_Block_Below(Master VARCHAR2)
	// RETURN VARCHAR2 IS
	// curblk VARCHAR2(30); -- Current Block
	// currel VARCHAR2(30); -- Current Relation
	// retblk VARCHAR2(30); -- Return Block
	// BEGIN
	// --
	// -- Initialize Local Vars
	// --
	// curblk := Master;
	// currel := Get_Block_Property(curblk, FIRST_MASTER_RELATION);
	// --
	// -- While there exists another relation for this block
	// --
	// WHILE currel IS NOT NULL LOOP
	// --
	// -- Get the name of the detail block
	// --
	// curblk := Get_Relation_Property(currel, DETAIL_NAME);
	// --
	// -- If this block has changes, return its name
	// --
	// IF ( Get_Block_Property(curblk, STATUS) = 'CHANGED' ) THEN
	// RETURN curblk;
	// ELSE
	// --
	// -- No changes, recursively look for changed blocks below
	// --
	// retblk := First_Changed_Block_Below(curblk);
	// --
	// -- If some block below is changed, return its name
	// --
	// IF retblk IS NOT NULL THEN
	// RETURN retblk;
	// ELSE
	// --
	// -- Constarer the next relation
	// --
	// currel := Get_Relation_Property(currel, NEXT_MASTER_RELATION);
	// END IF;
	// END IF;
	// END LOOP;
	//
	// --
	// -- No changed blocks were found
	// --
	// RETURN NULL;
	// END First_Changed_Block_Below;
	// */
	//
	// try
	// {
	// mastblk = system.getMasterBlock();
	// coordop = system.getCoordinationOperation();
	// trigblk = system.getTriggerBlock();
	// startitm = system.getCursorItem();
	// frmstat = system.getFormStatus();
	// if(!Arrays.asList("CLEAR_RECORD","SYNCHRONIZE_BLOCKS").contains(coordop)) {
	// if(Objects.equals(mastblk, trigblk)) {
	// if(Objects.equals(frmstat, "CHANGED")) {
	// //TODO curblk = firstChangedBlockBelow(mastblk);
	// if(!Objects.equals(curblk, null)) {
	// goBlock(curblk, "");
	//
	// //TODO Check_Package_Failure --- Program Unit Calling
	// checkPackageFailure();
	//
	// //TODO Clear_Block(NO_COMMIT);
	// if(!(Objects.equals(system.getFormStatus(), "QUERY") ||
	// Objects.equals(system.getBlockStatus(), "NEW"))) {
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	//
	// }
	//
	//
	//
	// }
	//
	//
	//
	// }
	//
	//
	//
	// }
	//
	//
	// //TODO currel = getBlockProperty(trigblk,firstMasterRelation);
	// while (!Objects.equals(currel, null))
	// {
	// //TODO curdtl = getRelationProperty(currel,detailName);
	// if(!Objects.equals(getBlockProperty(curdtl,status), "NEW")) {
	// goBlock(curdtl, "");
	//
	// //TODO Check_Package_Failure --- Program Unit Calling
	// checkPackageFailure();
	//
	// //TODO Clear_Block(NO_VALIDATE);
	// if(!Objects.equals(system.getBlockStatus(), "NEW")) {
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	//
	// }
	//
	//
	// //TODO currel = getRelationProperty(currel,nextMasterRelation);
	//
	// }
	// if(!Objects.equals(system.getCursorItem(), startitm)) {
	// goItem(startitm);
	//
	// //TODO Check_Package_Failure --- Program Unit Calling
	// checkPackageFailure();
	//
	// }
	//
	//
	// }
	// // Form_Trigger_Failure
	// catch(FormTriggerFailureException e)
	// {
	// if(!Objects.equals(system.getCursorItem(), startitm)) {
	// goItem(startitm);
	//
	// }
	//
	//
	//// TODO RAISE
	//
	// }
	//
	// log.info("clearAllMasterDetails Executed Successfully");
	// }
	// catch(Exception e){
	// log.error("Error while executing clearAllMasterDetails"+e.getMessage());
	// throw e;
	//
	// }
	// }
	//
	//
	@Override
	public popupateRecordrRes populateRecord(CrAirportProcedure pRecord, Integer pCycle, Integer pProcessingCycle)
			throws Exception {
		log.info("populateRecord Executing");
		// String query = "";
		// Record rec = null;
		try {

			if (Objects.equals(parameter.getRecordType(), "S")) {
				pProcessingCycle = toInteger(nvl(plStdStar.getProcessingCycle(), global.getProcessingCycle()));
//				OracleHelpers.integerGreaterThan(toInteger(global.getRecentCycle()), pProcessingCycle);
				if (OracleHelpers.integerGreaterThan(toInteger(global.getRecentCycle()), pProcessingCycle)) {
					pCycle = pProcessingCycle;

				}

				else {
					pCycle = toInteger(global.getRecentCycle());

				}
				pRecord.setProcedureType("E");
				pRecord.setRecordType(parameter.getRecordType());
				pRecord.setSection("P");
				pRecord.setAirportIcao(plStdStar.getAirportIcao());
				pRecord.setAirportIdent(plStdStar.getAirportIdent());
				pRecord.setAlt1(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAlt1(), " "));
				pRecord.setAlt2(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAlt2(), " "));
				pRecord.setAltDescription(
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAltDescription(), " "));
//				pRecord.setArcRadius(lpad(
//						toChar(plStdStarLeg.getRow(system.getCursorRecordIndex()).getArcRadius() == null ? null
//								:(int) Math.floor(plStdStarLeg.getRow(system.getCursorRecordIndex()).getArcRadius() * 1000)),
//						6, '0'));
				pRecord.setArcRadius(lpad(
						toString(OracleHelpers.getIntValue(
								asterisk(plStdStarLeg.getRow(system.getCursorRecordIndex()).getArcRadius(), 1000))),
						6, '0'));
				pRecord.setAtcInd(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAtcInd(), " "));
				pRecord.setCenterFixIcaoCode(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIcaoCode());
				pRecord.setCenterFixIdent(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIdent());
				pRecord.setCenterFixMultipleCode(
						plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixMultipleCode());
				pRecord.setCenterFixSection(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSection());
				pRecord.setCenterFixSubsection(
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSubsection(), " "));
				pRecord.setCycleData(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCycleData());
				pRecord.setFileRecno(toString(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFileRecno()));
				pRecord.setFixIcaoCode(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode());
				pRecord.setFixIdent(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent());
				pRecord.setFixSectionCode(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode());
				pRecord.setFixSubsectionCode(
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode(), " "));
				pRecord.setMagneticCourse(
						rpad(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse(), " "), 4));
				pRecord.setPathAndTermination(
						plStdStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination());
				pRecord.setRecommNavaidIcaoCode(
						plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIcaoCode());
				pRecord.setRecommNavaidIdent(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIdent());
				pRecord.setRecommNavaidSection(
						plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSection());
				pRecord.setRecommNavaidSubsection(
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSubsection(), " "));
				pRecord.setRho(lpad(
						toChar(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRho() == null ? null
								: OracleHelpers.getIntValue(
										asterisk(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRho(), 10))),
						4, '0'));
				pRecord.setRnp(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRnp());
				pRecord.setRouteDistance(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance());
				pRecord.setRouteType(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(), " "));
				pRecord.setSequenceNum(
						lpad(toString(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum()), 3, '0'));
				pRecord.setProcedureIdent(plStdStar.getStarIdent());
				pRecord.setSpeedLimit(
						lpad(toString(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSpeedLimit()), 3, '0'));
				pRecord.setTheta(lpad(
						toChar(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTheta() == null ? null
								: OracleHelpers.getIntValue(
										asterisk(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTheta(), 10))),
						4, '0'));
				pRecord.setTransAltitude(
						lpad(toChar(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransAltitude()), 5, '0'));
				pRecord.setTransitionIdent(
						rpad(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent(), 5, " "));
				pRecord.setTurnDirValid(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTurnDirValid(), " "));
				pRecord.setTurnDirection(
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTurnDirection(), " "));

				if (OracleHelpers.bigDecimalLesserThan(
						plStdStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), BigDecimal.valueOf(0))) {
					pRecord.setVerticalAngle("-" + lpad(
							substr(toChar(OracleHelpers.getIntValue(asterisk(
									plStdStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), 100))), 2),
							3, '0'));

				}

				else {
					pRecord.setVerticalAngle(lpad(
							toChar(OracleHelpers.getIntValue(asterisk(
									plStdStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), 100))),
							4, '0'));

				}
				pRecord.setWaypointDescCode(
						nvl(rpad(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), 4, " "),
								"    "));
				pRecord.setAreaCode(plStdStar.getAreaCode());
				pRecord.setSpecialsInd(plStdStar.getSpecialsInd());
				pRecord.setSpeedLimitDesc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSpeedLimitDesc());
				pRecord.setAircraftType(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType(), ""));
				pRecord.setProcDesignMagVar(
						rpad(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getProcDesignMagVar(), " "), 5));

			}

			else {
				pProcessingCycle = toInteger((nvl(plTldStar.getProcessingCycle(), global.getProcessingCycle())));
				pCycle = (pProcessingCycle);
				pRecord.setProcedureType("E");
				pRecord.setRecordType(parameter.getRecordType());
				pRecord.setSection("P");
				pRecord.setAirportIcao(plTldStar.getAirportIcao());
				pRecord.setAirportIdent(plTldStar.getAirportIdent());
				pRecord.setAlt1(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAlt1(), " "));
				pRecord.setAlt2(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAlt2(), " "));
				pRecord.setAltDescription(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAltDescription(), " "));

				pRecord.setArcRadius(lpad(
						toString(OracleHelpers.getIntValue(
								asterisk(plTldStarLeg.getRow(system.getCursorRecordIndex()).getArcRadius(), 1000))),
						6, '0'));
				pRecord.setAtcInd(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAtcInd(), " "));
				pRecord.setCenterFixIcaoCode(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIcaoCode());
				pRecord.setCenterFixIdent(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIdent());
				pRecord.setCenterFixMultipleCode(
						plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixMultipleCode());
				pRecord.setCenterFixSection(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSection());
				pRecord.setCenterFixSubsection(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSubsection(), " "));
				pRecord.setCycleData(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCycleData());
				pRecord.setFileRecno(toString(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFileRecno()));
				pRecord.setFixIcaoCode(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode());
				pRecord.setFixIdent(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent());
				pRecord.setFixSectionCode(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode());
				pRecord.setFixSubsectionCode(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode(), " "));
				pRecord.setMagneticCourse(
						rpad(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse(), " "), 4));
				pRecord.setPathAndTermination(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(), " "));
				pRecord.setRecommNavaidIcaoCode(
						plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIcaoCode());
				pRecord.setRecommNavaidIdent(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIdent());
				pRecord.setRecommNavaidSection(
						plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSection());
				pRecord.setRecommNavaidSubsection(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSubsection(), " "));

				pRecord.setRho(lpad(
						toChar(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRho() == null ? null
								: OracleHelpers.getIntValue(
										asterisk(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRho(), 10))),
						4, '0'));
				pRecord.setRnp(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRnp());
				pRecord.setRouteDistance(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance());
				pRecord.setRouteType(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(), " "));
				pRecord.setSequenceNum(
						lpad(toString(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum()), 3, '0'));
				pRecord.setProcedureIdent(plTldStar.getStarIdent());
				pRecord.setSpeedLimit(
						lpad(toString(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSpeedLimit()), 3, '0'));
				pRecord.setTheta(lpad(
						toChar(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTheta() == null ? null
								: OracleHelpers.getIntValue(
										asterisk(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTheta(), 10))),
						4, '0'));
				pRecord.setTransAltitude(
						lpad(toChar(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransAltitude()), 5, '0'));
				pRecord.setTransitionIdent(
						rpad(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent(), 5, " "));
				pRecord.setTurnDirValid(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTurnDirValid(), " "));
				pRecord.setTurnDirection(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTurnDirection(), " "));
				if (OracleHelpers.bigDecimalLesserThan(
						plTldStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), BigDecimal.valueOf(0))) {
					pRecord.setVerticalAngle("-" + lpad(
							substr(toChar(OracleHelpers.getIntValue(asterisk(
									plTldStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), 100))), 2),
							3, '0'));

				}

				else {
					pRecord.setVerticalAngle(lpad(
							toChar(OracleHelpers.getIntValue(asterisk(
									plTldStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), 100))),
							4, '0'));

				}
				pRecord.setWaypointDescCode(
						nvl(rpad(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), 4, " "),
								"    "));
				pRecord.setAreaCode(plTldStar.getCustomerIdent());
				pRecord.setSpecialsInd(plTldStar.getSpecialsInd());
				pRecord.setSpeedLimitDesc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSpeedLimitDesc());
				pRecord.setAircraftType(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType(), ""));
				pRecord.setProcDesignMagVar(
						rpad(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getProcDesignMagVar(), " "), 5));
				return new popupateRecordrRes(pRecord, pCycle);
			}

			log.info("populateRecord Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing populateRecord" + e.getMessage());
			throw e;

		}
		return new popupateRecordrRes(pRecord, pCycle);
	}

	//
	//
	@Override
	public populateRelRecordRes populateRelRecord(Integer pDcr, String pTable, RecProcRdv pRecord) throws Exception {
		log.info("populateRelRecord Executing");
		// String query = "";
		// Record rec = null;
		try {
			// TODO Configure the Out Params --> p_record
			// TODO Configure the Out Params --> p_table
			// TODO Configure the Out Params --> p_dcr

			if (Objects.equals(parameter.getRecordType(), "S")) {
				pTable = "pl_std_star_leg";
				pDcr = toInteger(plStdStar.getCreateDcrNumber());
				pRecord.setCfixicao(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIcaoCode());
				pRecord.setCfixid(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIdent());
				pRecord.setCfixsec(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSection());
				pRecord.setCfixsubsec(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSubsection());
				pRecord.setMultCode(
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixMultipleCode(), "0"));
				pRecord.setPathAndTerm(plStdStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination());
				pRecord.setFixicao(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode());
				pRecord.setFixid(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent());
				pRecord.setFixsec(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode());
				pRecord.setFixsubsec(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode());
				pRecord.setRecommicao(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIcaoCode());
				pRecord.setRecommid(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIdent());
				pRecord.setRecommsec(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSection());
				pRecord.setRecommsubsec(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSubsection());
				pRecord.setAirportIcao(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pRecord.setAirportIdent(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pRecord.setVProcedureIdent(plStdStar.getStarIdent());
				pRecord.setCustAreaCode(plStdStar.getAreaCode());
				pRecord.setGeneratedInHouseFlag("N");
				pRecord.setValidateInd(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd());
				pRecord.setVRowid(null);
				pRecord.setCdcr(toInteger(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				pRecord.setRouteType(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType());
				pRecord.setVProcedureIdent(plStdStarLeg.getRow(system.getCursorRecordIndex()).getStarIdent());
				pRecord.setWaypointDescCode(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode());
				pRecord.setMagneticCourse(plStdStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse());

			}

			else {
				pTable = "pl_tld_star_leg";
				pDcr = toInteger(plTldStar.getCreateDcrNumber());
				pRecord.setCfixicao(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIcaoCode());
				pRecord.setCfixid(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIdent());
				pRecord.setCfixsec(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSection());
				pRecord.setCfixsubsec(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixSubsection());
				pRecord.setMultCode(
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixMultipleCode(), "0"));
				pRecord.setPathAndTerm(plTldStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination());
				pRecord.setFixicao(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode());
				pRecord.setFixid(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent());
				pRecord.setFixsec(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode());
				pRecord.setFixsubsec(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode());
				pRecord.setRecommicao(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIcaoCode());
				pRecord.setRecommid(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidIdent());
				pRecord.setRecommsec(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSection());
				pRecord.setRecommsubsec(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecommNavaidSubsection());
				pRecord.setAirportIcao(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAirportIcao());
				pRecord.setAirportIdent(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAirportIdent());
				pRecord.setVProcedureIdent(plTldStar.getStarIdent());
				pRecord.setCustAreaCode(plTldStar.getCustomerIdent());
				pRecord.setGeneratedInHouseFlag(plTldStar.getGeneratedInHouseFlag());
				pRecord.setValidateInd(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd());
				pRecord.setVRowid(null);
				pRecord.setCdcr(toInteger(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				pRecord.setRouteType(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType());
				pRecord.setVProcedureIdent(plTldStarLeg.getRow(system.getCursorRecordIndex()).getStarIdent());
				pRecord.setWaypointDescCode(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode());
				pRecord.setMagneticCourse(plTldStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse());

			}

			log.info("populateRelRecord Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing populateRelRecord" + e.getMessage());
			throw e;

		}
		return new populateRelRecordRes(pDcr, pTable, pRecord);

	}

	//
	@Override
	public void validateLeg(String pIgnoreRef) throws Exception {
		log.info("validateLeg Executing");

		try {
			CrAirportProcedure vRecord = new CrAirportProcedure();
			Integer vErrInd = 0;
			List<Integer> vErrList = new ArrayList<Integer>();
			String vRerr = null;
			String vAllErr = null;
			String vValid = null;
			RecProcRdv vRelRec = new RecProcRdv();
			Integer vCycle = 0;
			Integer vProcessingCycle = 0;
			String vTable = null;
			Integer vDcr = 0;
			String vLegInd = "Y";

			popupateRecordrRes res = populateRecord(vRecord, vCycle, vProcessingCycle);
			vRecord = res.crAirportProcedure();
			vCycle = res.pCycle();
			;

			Map<String, Object> ou = app.executeProcedure("CPT", "VAIRPORT_PROCEDURE", "RECSV2",
					new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
					new ProcedureInParameter("p_proc_cycle", vCycle, OracleTypes.VARCHAR),
					new ProcedureInOutParameter("p_crecord", vRecord, OracleTypes.STRUCT,
							"SDV_RECORDS.CR_AIRPORT_PROCEDURE"),
					new ProcedureOutParameter("p_errlist", OracleTypes.ARRAY, "CPT_TYPES.ERRLIST_TYPE"),
					new ProcedureOutParameter("p_err_ind", OracleTypes.NUMBER));

			vErrInd = toInteger(ou.get("p_err_ind"));
			Struct outStruct = (Struct) ou.get("p_crecord");

			vRecord = OracleHelpers.mapStructToClass(outStruct, CrAirportProcedure.class);
			outStruct.getAttributes();
			Array errList = (Array) ou.get("p_errlist");
			BigDecimal[] bg = (BigDecimal[]) errList.getArray();

			for (BigDecimal itr1 : bg) {
				vErrList.add(itr1.intValue());
			}

			if (!Objects.equals(vErrInd, 0)) {
				for (int i = 0; i <= vErrList.size() - 1; i++) {
					if (!coreptLib.isOverride(global.getDataSupplier(), vCycle, "STAR", vErrList.get(i))) {
						vAllErr = getNullClean(vAllErr) + " * " + toChar(vErrList.get(i)) + " - "
								+ coreptLib.getErrText(vErrList.get(i));
						vLegInd = "I";
					}

					else {
						if (!Objects.equals(vLegInd, "I")) {
							vLegInd = "O";

						}
					}

				}

			}

			populateRelRecordRes res2 = populateRelRecord(vDcr, vTable, vRelRec);
			vDcr = res2.pDcr();
			vTable = res2.pTable();
			vRelRec = res2.pRecord();
			Map<String, Object> dbCall1 = app.executeProcedure("CPT", "VAIRPORT_PROCEDURE", "RECRV2",
					new ProcedureInParameter("p_table", vTable, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
					new ProcedureInParameter("p_proc_cycle", vCycle, OracleTypes.NUMBER),
					new ProcedureInParameter("p_record_type", parameter.getRecordType(), OracleTypes.VARCHAR),
					new ProcedureInParameter("p_rec", vRelRec, OracleTypes.STRUCT, "CPT_TYPES.REC_PROC_RDV"),
					new ProcedureInParameter("p_dcr", vDcr, OracleTypes.NUMBER),
					new ProcedureOutParameter("p_err", OracleTypes.VARCHAR),
					new ProcedureOutParameter("p_valind", OracleTypes.VARCHAR),
					new ProcedureInParameter("p_view_only", pIgnoreRef, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_run_env", null, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_run_loc", "DU", OracleTypes.VARCHAR));
			// TODO
			vRerr = toString(dbCall1.get("p_err"));

			// coverity-fixes
			vValid = toString(dbCall1.get("p_valind"));
			log.info(vValid);

			if (like("%N%", vRerr)) {
				if (Objects.equals(substr(vRerr, 2, 1), "N")) {
					vAllErr = getNullClean(vAllErr) + " * 475 - " + coreptLib.getErrText(475);

				}

				if (Objects.equals(substr(vRerr, 3, 1), "N")) {
					vAllErr = getNullClean(vAllErr) + " * 476 - " + coreptLib.getErrText(476);

				}

				if (Objects.equals(substr(vRerr, 4, 1), "N")) {
					vAllErr = getNullClean(vAllErr) + " * 477 - " + coreptLib.getErrText(477);

				}

				if (Objects.equals(substr(vRerr, 5, 1), "N")) {
					vAllErr = getNullClean(vAllErr) + " * 467 - " + coreptLib.getErrText(467);

				}

				if (Objects.equals(substr(vRerr, 8, 1), "N")) {
					vAllErr = getNullClean(vAllErr) + " * 1167 - " + coreptLib.getErrText(1167);

				}

			}

			if (Objects.equals(pIgnoreRef, "N")) {

				setUpdateDcr(upper(system.getCursorBlock()), Optional.ofNullable(vRecord));

			}

			if (!OracleHelpers.isNullorEmpty(vAllErr)) {
				controlBlock.setSeqErrors(
						"* Route Type '" + vRecord.getRouteType() + "', Transition '" + vRecord.getTransitionIdent()
								+ "': " + chr(10) + "Seq#" + vRecord.getSequenceNum() + " ->" + vAllErr + chr(10));

				if (Objects.equals(parameter.getRecordType(), "S")) {
					setItemProperty("control_block.std_leg_errors", VISIBLE, PROPERTY_TRUE);
					setItemProperty("control_block.std_leg_errors", ENABLED, PROPERTY_TRUE);
					controlBlock.setStdLegErrors(getNullClean(vAllErr));
					if (!Objects.equals(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "N"),
							"I")) {
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd("I");

					}

					if (Objects.equals(plStdStar.getRefInfo(), null)) {
						if (Arrays.asList("Y", "S", "H", "W", "N", "O")
								.contains(nvl(plStdStar.getValidateInd(), "Y"))) {
							plStdStar.setValidateInd("I");
							if (Objects.equals(plStdStar.getRecordStatus(), "QUERIED"))
								plStdStar.setRecordStatus("CHANGED");
							plStdStar.setOldValidateInd("I");
							controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);

						}

					}

					else {
						controlBlock.setStdValidationErrors(plStdStar.getRefInfo());
						setItemProperty("control_block.std_validation_errors", VISIBLE, PROPERTY_TRUE);
						setItemProperty("control_block.std_validation_errors", ENABLED, PROPERTY_TRUE);

					}

				}

				else {
					setItemProperty("control_block.tld_leg_errors", VISIBLE, PROPERTY_TRUE);
					setItemProperty("control_block.tld_leg_errors", ENABLED, PROPERTY_TRUE);
					controlBlock.setTldLegErrors(getNullClean(vAllErr));
					if (!Objects.equals(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "N"),
							"I")) {
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd("I");

					}

					if (Objects.equals(plTldStar.getRefInfo(), null)) {
						if (Arrays.asList("Y", "S", "H", "W", "N", "O")
								.contains(nvl(plTldStar.getValidateInd(), "Y"))) {
							plTldStar.setValidateInd("I");
							if (Objects.equals(plTldStar.getRecordStatus(), "QUERIED"))
								plTldStar.setRecordStatus("CHANGED");
							plTldStar.setOldValidateInd("I");
							controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);

						}

					}

					else {
						controlBlock.setTldValidationErrors(plTldStar.getRefInfo());
						setItemProperty("control_block.tld_validation_errors", VISIBLE, PROPERTY_TRUE);
						setItemProperty("control_block.tld_validation_errors", ENABLED, PROPERTY_TRUE);

					}

				}

			}

			else {
				controlBlock.setSeqErrors(null);
				if (Objects.equals(parameter.getRecordType(), "S")) {
					if (Objects.equals(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "I"),
							"I")) {
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(vLegInd);

					}

					else {
						if ((Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "Y")
								&& Objects.equals(vLegInd, "O"))
								|| (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(),
										"O") && Objects.equals(vLegInd, "Y"))) {
							plStdStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(vLegInd);

						}

					}
					setItemProperty("control_block.std_leg_errors", VISIBLE, PROPERTY_FALSE);

				}

				else if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "I"),
							"I")) {
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(vLegInd);

					}

					else {
						if ((Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "Y")
								&& Objects.equals(vLegInd, "O"))
								|| (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(),
										"O") && Objects.equals(vLegInd, "Y"))) {
							plTldStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(vLegInd);

						}

					}
					setItemProperty("control_block.tld_leg_errors", VISIBLE, PROPERTY_FALSE);

				}

			}

			log.info("validateLeg Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing validateLeg" + e.getMessage());
			throw e;

		}
	}

	public void validateBeforeCommit(AirportStarTriggerRequestDto reqDto) throws Exception {
		int oldIndex = system.getCursorRecordIndex();
		Block<?> recstdseg = ((Block<?>) nameIn(this, "PL_STD_STAR_SEGMENT"));
		system.setCursorRecordIndex(0);
		if (Objects.equals(system.getCursorBlock(), "PL_STD_STAR_SEGMENT")) {
			for (Object rec : recstdseg.getData()) {
				if (Objects.equals(nameIn(rec, "recordStatus"), "DELETED")) {
					this.plStdStarSegment.filterNonDeletedRecords();

					plStdStarSegmentWhenValidateRecord(reqDto);
					plStdStarLegWhenValidateRecord(reqDto);
				}
				system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
			}
			system.setCursorRecordIndex(0);
			plStdStarWhenNewRecordInstance(reqDto);
		}
		Block<?> rectldseg = ((Block<?>) nameIn(this, "PL_TLD_STAR_SEGMENT"));
		system.setCursorRecordIndex(0);
		if (Objects.equals(system.getCursorBlock(), "PL_TLD_STAR_SEGMENT")) {
			for (Object rec : rectldseg.getData()) {
				if (Objects.equals(nameIn(rec, "recordStatus"), "DELETED")) {
					this.plTldStarSegment.filterNonDeletedRecords();

					plTldStarSegmentWhenValidateRecord(reqDto);
					plTldStarLegWhenValidateRecord(reqDto);
				}
				system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
			}
			system.setCursorRecordIndex(0);
			plTldStarWhenNewRecordInstance(reqDto);
		}
		system.setCursorRecordIndex(oldIndex);
	}

	public void cycledata() {
		Boolean val = false;
		if (Objects.equals(parameter.getRecordType(), "S")) {
			Block<?> recs = ((Block<?>) nameIn(this, "PL_STD_STAR_LEG"));
			system.setCursorRecordIndex(0);
			for (Object rec : recs.getData()) {
				for (PlStdStarLeg leg : plStdStarLeg.getData()) {
					if (!Objects.equals(leg.getRouteType(), null) || !Objects.equals(leg.getTransitionIdent(), null)) {

						if (Arrays.asList("NEW", "INSERT", "CHANGED").contains(leg.getRecordStatus())) {
							val = true;
							if (Objects.equals(nameIn(rec, "cycleData"), null)
									|| Objects.equals(nameIn(rec, "updateDcrNumber"), null)) {
								copy(toInteger(global.getDcrNumber()), "pl_std_star_leg.update_dcr_number");
								copy(toInteger(global.getDcrNumber()), "pl_std_star" + ".update_dcr_number");
								plStdStar.setRecordStatus("CHANGED");
							}

						}
					}
				}

				if (Objects.equals(val, true)) {
					if (!Arrays.asList("NEW", "INSERT").contains(plStdStar.getRecordStatus())) {
						plStdStar.setRecordStatus("CHANGED");
					}
				}
				system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
			}
		} else {
			Block<?> recs = ((Block<?>) nameIn(this, "PL_TLD_STAR_LEG"));
			system.setCursorRecordIndex(0);
			for (Object rec : recs.getData()) {
				for (PlTldStarLeg leg : plTldStarLeg.getData()) {
					if (!Objects.equals(leg.getRouteType(), null) || !Objects.equals(leg.getTransitionIdent(), null)) {
						if (Arrays.asList("NEW", "INSERT", "CHANGED").contains(leg.getRecordStatus())) {
							val = true;
							if (Objects.equals(nameIn(rec, "cycleData"), null)
									|| Objects.equals(nameIn(rec, "updateDcrNumber"), null)) {
								copy(toInteger(global.getDcrNumber()), "pl_tld_star_leg.update_dcr_number");
								copy(toInteger(global.getDcrNumber()), "pl_tld_star" + ".update_dcr_number");
								plTldStar.setRecordStatus("CHANGED");
							}

						}
					}
				}
				if (Objects.equals(val, true)) {
					if (!Arrays.asList("NEW", "INSERT").contains(plTldStar.getRecordStatus())) {
						plTldStar.setRecordStatus("CHANGED");
					}
					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
				}
			}
		}
	}

	@Override
	public void validateProcedure(String pIgnoreRef) throws Exception {
		log.info("validateProcedure Executing");

		int prevCursorRecordIndex = system.getCursorRecordIndex();
		try {
			CrAirportProcedure vRecord = new CrAirportProcedure();
			String vAllErr = null;
			String vProcErr = null;
			String cursorBlock = system.getCursorBlock();

			String vDetailInd = "------";
			String vMasterInd = "------";
			List<String> vSegs = new ArrayList<String>();
			String vSegExist = "N";
			Integer vIndex = 0;
			Integer vCycle = 0;
			String lsValidInd = null;

			if (Objects.equals(parameter.getRecordType(), "S")) {
				controlBlock.setStdOverrideErrors(null);

			}

			else {
				controlBlock.setTldOverrideErrors(null);

			}

			validateKeys(pIgnoreRef);
			vAllErr = getNullClean(controlBlock.getKeyErrors());
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {

				this.refreshMasterLibrary.deleteFromRefTable(
						toInteger(nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".create_dcr_number")), null);

			}

			if (Objects.equals(system.getFormStatus(), "QUERY")) {
				vAllErr = validateRwyTransition() + getNullClean(vAllErr);

			}

			if (Objects.equals(parameter.getRecordType(), "S")) {
				setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_TRUE);
				setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
						FormConstant.PROPERTY_TRUE);
				controlBlock.setStdValidationErrors("W O R K I N G !  Please wait.");
				vMasterInd = setValidateInd(plStdStar.getValidateInd(), vMasterInd);
				if (!like("%SEGMENT", cursorBlock)) {

				}

				system.setCursorRecordIndex(0);
				if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(), null)) {
					for (int i = 0; i < plStdStarSegment.size(); i++) {
						system.setCursorRecordIndex(i);
						if (Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1(),
								null)) {

						}

						else if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1(),
								null)
								&& !Arrays.asList("D", "R", "F", "G", "H", "P").contains(
										plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1())) {
							vAllErr = getNullClean(vAllErr) + " * Route Type: "
									+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()
									+ " Transition Ident: "
									+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()
									+ "-->" + "Invalid Qualifier_1: "
									+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1() + chr(10);

						}

						if (Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2(),
								null)) {
							// null;

						}

						else if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2(),
								null)
								&& !Arrays.asList("C", "D", "F", "G", "H", "I").contains(
										plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2())) {
							vAllErr = getNullClean(vAllErr) + " * Route type: "
									+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()
									+ " Transition Ident: "
									+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()
									+ "-->" + "Invalid Qualifier_2: "
									+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2() + chr(10);

						}

						if (Objects.equals(
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd(),
								null)) {
							// null;

						}

						else if (!Objects.equals(
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd(), null)
								&& !Arrays.asList("P", "L").contains(plStdStarSegment
										.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd())) {
							vCycle = toInteger(plStdStar.getProcessingCycle());
							if (!coreptLib.isOverride(global.getDataSupplier(), vCycle, "STAR", 1188)) {
								vAllErr = getNullClean(vAllErr) + " * " + "1188" + " - " + "Route Type: "
										+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()
										+ " Transition Ident: "
										+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()
										+ ":" + toString(coreptLib.getErrText(1188)) + chr(10);

							}

						}

						if (!Objects.equals(plStdStar.getProcessingCycle(),
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getProcessingCycle())) {
							plStdStarSegment.getRow(system.getCursorRecordIndex())
									.setProcessingCycle(plStdStar.getProcessingCycle());
						}

						vIndex = vIndex + 1;
						vSegs.add(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType() + ","
								+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent() + ","
								+ plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
						if (Objects.equals(pIgnoreRef, "N")) {

							setUpdateDcr("PL_STD_STAR_SEGMENT", Optional.ofNullable(vRecord));

						}

						// nextRecord( "");

					}

				}

				app.executeProcedure("recsv2", "vset_global_constant", null,
						new ProcedureInParameter("p_nErrNum", 1166, OracleTypes.VARCHAR));

				magneticCourseSdvPrc(parameter.getRecordType());
				system.setCursorRecordIndex(0);
				if (!Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
					if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {

						for (int i = 0; i < plStdStarLeg.size(); i++) {
							system.setCursorRecordIndex(i);
							if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(),
									controlBlock.getOldRouteType())
									&& Objects.equals(
											plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
											controlBlock.getOldTransitionIdent())
									&& Objects.equals(
											plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType(),
											controlBlock.getOldAircraftType())) {
								plStdStarLeg.getRow(system.getCursorRecordIndex())
										.setRouteType(controlBlock.getNewRouteType());
								plStdStarLeg.getRow(system.getCursorRecordIndex())
										.setTransitionIdent(controlBlock.getNewTransitionIdent());
								plStdStarLeg.getRow(system.getCursorRecordIndex())
										.setAircraftType(controlBlock.getNewAircraftType());

								plStdStarLeg.getRow(i).setRecordStatus("CHANGED");
							}

							// nextRecord( "");

						}
						controlBlock.setPkSegmentChange("N");
						controlBlock.setPkRouteChange("N");
						controlBlock.setPkTransitionChange("N");
						controlBlock.setPkAircraftChange("N");

					}

					String curBlock = system.getCursorBlock();
					for (int K = 0; K < plStdStarLeg.size(); K++) {
						system.setCursorRecordIndex(K);
						if (!Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRecordStatus(),
								"QUERIED"))
							system.setCursorBlock("PL_STD_STAR_LEG");
						validateLeg(pIgnoreRef);
						vSegExist = "N";
						for (int i = 0; i < vIndex; i++) {
							if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType() + ","
									+ plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent() + ","
									+ plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType(),
									vSegs.get(i))) {
								vSegExist = "Y";
								break;

							}

						}
						if (Objects.equals(vSegExist, "N")) {
							if (Objects.equals(vAllErr, null) || length(vAllErr) < 30000) {
								vAllErr = getNullClean(vAllErr) + " * Segment "
										+ plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType()
										+ " Transition Ident "
										+ plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent()
										+ " Aircraft Type "
										+ plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType() + "Seq #"
										+ plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum() + "--> "
										+ toChar(5630) + " - " + coreptLib.getErrText(5630) + chr(10);

							}

						}

						vAllErr = substr(getNullClean(vAllErr) + getNullClean(controlBlock.getSeqErrors()), 1, 30000);
						vDetailInd = setValidateInd(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(),
								vDetailInd);
						// nextRecord( "");

					}
					system.setCursorBlock(curBlock);
					if (toInteger(global.getRecentCycle()) >= toInteger(plStdStar.getProcessingCycle())) {
						vCycle = toInteger(plStdStar.getProcessingCycle());

					}

					else {
						vCycle = toInteger(global.getRecentCycle());

					}
					vProcErr = vOneProc("S", "STAR", "A", plStdStar.getAirportIdent(), plStdStar.getAirportIcao(),
							plStdStar.getStarIdent(), plStdStar.getAreaCode(),
							toInteger(plStdStar.getCreateDcrNumber()), plStdStar.getDataSupplier(), vCycle, vProcErr,
							pIgnoreRef);

				}

				else {
					vAllErr = getNullClean(vAllErr) + " ** This Procedure has no legs! ";

				}

			}

			else {
				setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_TRUE);
				setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
						FormConstant.PROPERTY_TRUE);
				vMasterInd = setValidateInd(plTldStar.getValidateInd(), vMasterInd);
				if (!like("%SEGMENT", system.getCursorBlock())) {

				}

				system.setCursorRecordIndex(0);
				if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(), null)) {
					for (int i = 0; i < plTldStarSegment.size(); i++) {
						system.setCursorRecordIndex(i);
						if (Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1(),
								null)) {
							// null;

						}

						else if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1(),
								null)
								&& !Arrays.asList("D", "R", "F", "G", "H", "P").contains(
										plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1())) {
							vAllErr = getNullClean(vAllErr) + " * Route Type: "
									+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()
									+ " Transition Ident: "
									+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()
									+ "-->" + "Invalid Qualifier_1: "
									+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1() + chr(10);

						}

						if (Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2(),
								null)) {
							// null;

						}

						else if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2(),
								null)
								&& !Arrays.asList("C", "D", "F", "G", "H", "I").contains(
										plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2())) {
							vAllErr = getNullClean(vAllErr) + " * Route type: "
									+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()
									+ " Transition Ident: "
									+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()
									+ "-->" + "Invalid Qualifier_2: "
									+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2() + chr(10);

						}

						if (Objects.equals(
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd(),
								null)) {
							// null;

						}

						else if (!Objects.equals(
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd(), null)
								&& !Arrays.asList("P", "L").contains(plTldStarSegment
										.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd())) {
							vCycle = toInteger(plTldStar.getProcessingCycle());
							if (!coreptLib.isOverride(global.getDataSupplier(), vCycle, "STAR", 1188)) {
								vAllErr = getNullClean(vAllErr) + " * " + "1188" + " - " + "Route Type: "
										+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()
										+ " Transition Ident: "
										+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()
										+ ":" + coreptLib.getErrText(1188) + chr(10);

							}

						}

						if (Objects.equals(plTldStar.getFlagChange(), "Y")) {
							plTldStarSegment.getRow(system.getCursorRecordIndex())
									.setGeneratedInHouseFlag(plTldStar.getGeneratedInHouseFlag());
							plTldStarSegment.getRow(system.getCursorRecordIndex())
									.setProcessingCycle(plTldStar.getProcessingCycle());

						}

						vIndex = vIndex + 1;
						vSegs.add(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType() + ","
								+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent() + ","
								+ plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
						if (Objects.equals(pIgnoreRef, "N")) {

							setUpdateDcr("PL_TLD_STAR_SEGMENT", null);

						}

					}

				}

				app.executeProcedure("recsv2", "vset_global_constant", null,
						new ProcedureInParameter("p_nErrNum", 1166, OracleTypes.VARCHAR));

				magneticCourseSdvPrc(parameter.getRecordType());
				system.setCursorRecordIndex(0);
				if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
					if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")
							|| Objects.equals(plTldStar.getFlagChange(), "Y")) {

						for (int i = 0; i < plTldStarLeg.size(); i++) {
							system.setCursorRecordIndex(i);
							if (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(),
									controlBlock.getOldRouteType())
									&& Objects.equals(
											plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
											controlBlock.getOldTransitionIdent())
									&& Objects.equals(
											plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType(),
											controlBlock.getOldAircraftType())) {
								plTldStarLeg.getRow(system.getCursorRecordIndex())
										.setRouteType(controlBlock.getNewRouteType());
								plTldStarLeg.getRow(system.getCursorRecordIndex())
										.setTransitionIdent(controlBlock.getNewTransitionIdent());
								plTldStarLeg.getRow(system.getCursorRecordIndex())
										.setAircraftType(controlBlock.getNewAircraftType());
								plTldStarLeg.getRow(system.getCursorRecordIndex()).setRecordStatus("CHANGED");

							}

							plTldStarLeg.getRow(system.getCursorRecordIndex())
									.setGeneratedInHouseFlag(plTldStar.getGeneratedInHouseFlag());
							plTldStarLeg.getRow(system.getCursorRecordIndex())
									.setProcessingCycle(plTldStar.getProcessingCycle());

						}
						controlBlock.setPkSegmentChange("N");
						controlBlock.setPkRouteChange("N");
						controlBlock.setPkTransitionChange("N");
						controlBlock.setPkAircraftChange("N");
						plTldStar.setFlagChange("N");

					}
					try {
						// TODO first_record;
						String curBlock = system.getCursorBlock();
						for (int K = 0; K < plTldStarLeg.size(); K++) {

							system.setCursorRecordIndex(K);
							if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRecordStatus(),
									"QUERIED"))
								system.setCursorBlock("PL_TLD_STAR_LEG");
							validateLeg(pIgnoreRef);
							vSegExist = "N";
							for (int i = 0; i < vIndex; i++) {
								if (Objects.equals(
										plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType() + ","
												+ plTldStarLeg.getRow(system.getCursorRecordIndex())
														.getTransitionIdent()
												+ ","
												+ plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType(),
										vSegs.get(i))) {
									vSegExist = "Y";
									break;

								}

							}
							if (Objects.equals(vSegExist, "N")) {
								if (OracleHelpers.isNullorEmpty(vAllErr) || length(vAllErr) < 30000) {
									vAllErr = getNullClean(vAllErr) + " * Route "
											+ plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType()
											+ " Transition "
											+ plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent()
											+ " Aircraft Type "
											+ plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType()
											+ ", Seq #"
											+ plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum()
											+ "--> " + toChar(5630) + " - " + coreptLib.getErrText(5630) + chr(10);

								}

							}

							vAllErr = substr(getNullClean(vAllErr) + getNullClean(controlBlock.getSeqErrors()), 1,
									30000);
							vDetailInd = setValidateInd(
									plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), vDetailInd);
							lsValidInd = getNullClean(lsValidInd)
									+ plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd();

						}
						system.setCursorBlock(curBlock);
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
					vCycle = toInteger(nvl(plTldStar.getProcessingCycle(), global.getProcessingCycle()));

					// TODO
					vProcErr = vOneProc("T", "STAR", "A", plTldStar.getAirportIdent(), plTldStar.getAirportIcao(),
							plTldStar.getStarIdent(), plTldStar.getCustomerIdent(),
							toInteger(plTldStar.getCreateDcrNumber()), plTldStar.getDataSupplier(), vCycle, vProcErr,
							pIgnoreRef);

				}

				else {
					vAllErr = getNullClean(vAllErr) + " ** This Procedure has no legs! ";

				}

			}
			if (Objects.equals(controlBlock.getTempField(), "Y") && instr(lsValidInd, "I") > 0) {
				vAllErr = getNullClean(vAllErr)
						+ "* This STAR has been locked by other user. Please exit from the form without saving "
						+ chr(10);
				controlBlock.setTempField("N");

			}

			if (!Objects.equals(length(nvl(vAllErr, 0)), null)) {
				if (length(vAllErr + vProcErr) < 30000) {
					vAllErr = getNullClean(vAllErr) + getNullClean(vProcErr);

				}

				else if (length(vAllErr + vProcErr) >= 30000) {
					vAllErr = substr(getNullClean(vAllErr) + getNullClean(vProcErr), 1, 30000) + ".....";

				}

			}

			if (!like("%STAR", cursorBlock)) {

				goRecord(cursorBlock, system.getCursorRecordIndex());
				system.setCursorRecordIndex(0);
			}

			if (!Objects.equals(rtrim(toString(nameIn(this, substr(cursorBlock, 1, 11) + "_leg.sequence_num"))),
					null)) {

				system.setCursorRecordIndex(0);
				validateLeg("Y");

			}

			coreptLib.setMaster(pIgnoreRef, substr(cursorBlock, 1, 11), getNullClean(vAllErr), parameter.getWorkType(),
					vMasterInd, vDetailInd);
			controlBlock.setValidated("Y");

			log.info("validateProcedure Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing validateProcedure" + e.getMessage());
			throw e;

		} finally {
			system.setCursorRecordIndex(prevCursorRecordIndex);
		}
	}

	//
	@Override
	public void SetMessage(String pShow) throws Exception {
		log.info("setMessage Executing");
		// String query = "";
		// Record rec = null;
		try {

			if (Objects.equals(pShow, "Y")) {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
							FormConstant.PROPERTY_TRUE);
					setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
							FormConstant.PROPERTY_TRUE);

				}

				else {
					setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
							FormConstant.PROPERTY_TRUE);
					setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
							FormConstant.PROPERTY_TRUE);

				}

			}

			else {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					controlBlock.setStdValidationErrors(null);
					setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
							FormConstant.PROPERTY_FALSE);
					setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
							FormConstant.PROPERTY_FALSE);

				}

				else {
					controlBlock.setTldValidationErrors(null);
					setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
							FormConstant.PROPERTY_FALSE);
					setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
							FormConstant.PROPERTY_FALSE);

				}

			}

			// TODO synchronize;

			log.info("setMessage Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing setMessage" + e.getMessage());
			throw e;

		}
	}

	//
	//
	// @Override
	// public void validateKeys(String pIgnoreRef) throws Exception{
	// log.info("validateKeys Executing");
	// String query = "";
	// Record rec = null;
	// try {
	// Integer vDcr = 0;
	// Integer vErr = 0;
	// String vAllErrs = null;
	// Integer vCycle = 0;
	// Integer vProcessingCycle = 0;
	//
	//
	// //TODO Set_Application_Property(cursor_style,"BUSY");
	// if(Objects.equals(parameter.getRecordType(), "S")) {
	// if(Objects.equals(rtrim(plStdStar.getProcessingCycle()), null)) {
	// plStdStar.setProcessingCycle(global.getProcessingCycle());
	//
	// }
	//
	//
	// vProcessingCycle = plStdStar.getProcessingCycle();
	// if(global.getRecentCycle() >= vProcessingCycle) {
	// vCycle = vProcessingCycle;
	//
	// }
	//
	//
	// else {
	// vCycle = global.getRecentCycle();
	//
	// }
	//// TODO v_err := util1.v_area_code(:pl_std_star.area_code)
	//// TODO v_dcr := vrel2.check_reference(p_ignore_ref,
	// 'pl_std_star',:pl_std_star.create_dcr_number, 'S','P','A',
	// :global.data_supplier, v_cycle,--:pl_std_star.processing_cycle,
	// :pl_std_star.airport_icao, :pl_std_star.airport_ident)
	// if(Objects.equals(pIgnoreRef, "N")) {
	//
	// //TODO set_update_dcr('PL_STD_STAR',null) --- Program Unit Calling
	// setUpdateDcr("PL_STD_STAR",null);
	//
	// }
	//
	//
	//
	// }
	//
	//
	// else {
	//
	// //TODO
	// validate_extra_fields(plTldStar.getProcessingCycle(),plTldStar.getGeneratedInHouseFlag());
	// if(Objects.equals(rtrim(plTldStar.getProcessingCycle()), null)) {
	// plTldStar.setProcessingCycle(global.getProcessingCycle());
	//
	// }
	//
	//
	// vProcessingCycle = plTldStar.getProcessingCycle();
	// vCycle = vProcessingCycle;
	// if(Objects.equals(rtrim(plTldStar.getGeneratedInHouseFlag()), null)) {
	// plTldStar.setGeneratedInHouseFlag("Y");
	//
	// }
	//
	//
	//// TODO v_err := util1.v_customer(:pl_tld_star.customer_ident)
	//// TODO v_dcr := vrel2.check_reference(p_ignore_ref, 'pl_tld_star',
	// :pl_tld_star.create_dcr_number, 'T','P','A', :global.data_supplier, v_cycle,
	// --:pl_tld_star.processing_cycle, :pl_tld_star.airport_icao,
	// :pl_tld_star.airport_ident, :pl_tld_star.customer_ident, null,null,
	// :pl_tld_star.generated_in_house_flag, null,'DU')
	// if(Objects.equals(pIgnoreRef, "N")) {
	//
	// //TODO set_update_dcr('PL_TLD_STAR',null) --- Program Unit Calling
	// setUpdateDcr("PL_TLD_STAR",null);
	//
	// }
	//
	//
	//
	// }
	// if(vErr > 0 || Objects.equals(vDcr, null)) {
	// if(vErr > 0) {
	// if(!isOverride(global.getDataSupplier(),vProcessingCycle,"STAR",vErr)) {
	// //TODO vAllErrs = "* " + to_char(v_err) + " - " + getErrText(vErr);
	//
	// }
	//
	//
	//
	// }
	//
	//
	// if(Objects.equals(vDcr, null)) {
	// //TODO vAllErrs = "* 216 - " + getErrText(216);
	//
	// }
	//
	//
	// controlBlock.setKeyErrors(vAllErrs + chr(10));
	//
	// //TODO set_message('Y') --- Program Unit Calling
	// setMessage("Y");
	// if(Objects.equals(parameter.getRecordType(), "S")) {
	// controlBlock.setStdValidationErrors(vAllErrs + plStdStar.getRefInfo());
	// if(Objects.equals(plStdStar.getRefInfo(), null)) {
	// if(Arrays.asList("Y","S","H","W","N","O").contains(nvl(plStdStar.getValidateInd(),
	// "Y"))) {
	// plStdStar.setValidateInd("I");
	// plStdStar.setOldValidateInd("I");
	// controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);
	//
	// }
	//
	//
	//
	// }
	//
	//
	// else {
	// setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
	// FormConstant.PROPERTY_TRUE);
	// setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
	// FormConstant.PROPERTY_TRUE);
	//
	// //TODO Set_Application_Property(cursor_style,"DEFAULT");
	//
	// }
	//
	// }
	//
	//
	// else {
	// controlBlock.setTldValidationErrors(vAllErrs + plTldStar.getRefInfo());
	// if(Objects.equals(plTldStar.getRefInfo(), null)) {
	// if(Arrays.asList("Y","S","H","W","N","O").contains(nvl(plTldStar.getValidateInd(),
	// "Y"))) {
	// plTldStar.setValidateInd("I");
	// plTldStar.setOldValidateInd("I");
	// controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);
	//
	// }
	//
	//
	//
	// }
	//
	//
	// else {
	// setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
	// FormConstant.PROPERTY_TRUE);
	// setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
	// FormConstant.PROPERTY_TRUE);
	//
	// //TODO Set_Application_Property(cursor_style,"DEFAULT");
	//
	// }
	//
	// }
	//
	// }
	//
	//
	// else {
	// controlBlock.setKeyErrors(null);
	//
	// //TODO set_message('N') --- Program Unit Calling
	// setMessage("N");
	//
	// }
	//
	// //TODO Set_Application_Property(cursor_style,"DEFAULT");
	//
	// log.info("validateKeys Executed Successfully");
	// }
	// catch(Exception e){
	// log.error("Error while executing validateKeys"+e.getMessage());
	// throw e;
	//
	// }
	// }
	//
	//
	@Override
	public Boolean checkProcPk() throws Exception {
		log.info("checkProcPk Executing");
		String query = "";
		Record rec = null;
		try {
			Integer vExist = 0;

			if (Objects.equals(upper(parameter.getRecordType()), "S")) {

				query = """
						select count(*)
						  	from   pl_std_star
						  	where  airport_ident    = ?
						  	and    airport_icao     = ?
						  	and    star_ident       = ?
						  	and    processing_cycle = ?
						  	and    data_supplier    = ?
						  	and    create_dcr_number != ?
						""";
				rec = app.selectInto(query, plStdStar.getAirportIdent(), plStdStar.getAirportIcao(),
						plStdStar.getStarIdent(), plStdStar.getProcessingCycle(), plStdStar.getDataSupplier(),
						toString(nvl(plStdStar.getCreateDcrNumber(), 0)));
				vExist = rec.getInt();

			}

			else {

				query = """
						select count(*)
						  	from   pl_tld_star
						  	where  airport_ident    = ?
						  	and    airport_icao     = ?
						  	and    star_ident       = ?
						  	and    customer_ident   = ?
						  	and    processing_cycle = ?
						  	and    data_supplier    = ?
						  	and    create_dcr_number != ?
						""";
				rec = app.selectInto(query, plTldStar.getAirportIdent(), plTldStar.getAirportIcao(),
						plTldStar.getStarIdent(), plTldStar.getCustomerIdent(), plTldStar.getProcessingCycle(),
						plTldStar.getDataSupplier(), toString(nvl(plTldStar.getCreateDcrNumber(), 0)));
				vExist = rec.getInt();

			}
			if (vExist > 0) {

				return false;

			}

			log.info("checkProcPk Executed Successfully");
			return true;
		} catch (Exception e) {
			log.error("Error while executing checkProcPk" + e.getMessage());
			throw e;

		}
	}

	//
	@Override
	public Boolean checkSegmentPk() throws Exception {
		log.info("checkSegmentPk Executing");
		String query = "";
		Record rec = null;
		try {
			Integer vExist = 0;

			if (Objects.equals(parameter.getRecordType(), "S")) {

				query = """
						select count(*)
						  	from   pl_std_star_segment
						  	where  route_type       = ?
						  	and    transition_ident = ?
						  	and    aircraft_type = ? --65347
						  	and    airport_ident    = ?
						  	and    airport_icao     = ?
						  	and    star_ident       = ?
						  	and    processing_cycle = ?
						  	and    data_supplier    = ?
						  	and    create_dcr_number != ?
						""";
				rec = app.selectInto(query, plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getAirportIdent(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getAirportIcao(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getStarIdent(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getProcessingCycle(),
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getDataSupplier(),
						toInteger(plStdStarSegment.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				vExist = rec.getInt();

			}

			else {

				query = """
						select count(*)
						  	from   pl_tld_star_segment
						  	where  route_type       = ?
						  	and    transition_ident = ?
						  	and    aircraft_type = ? --65347
						  	and    airport_ident    = ?
						  	and    airport_icao     = ?
						  	and    star_ident       = ?
						  	and    customer_ident   = ?
						  	and    processing_cycle = ?
						  	and    data_supplier    = ?
						  	and    create_dcr_number != ?
						""";
				rec = app.selectInto(query, plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getAirportIdent(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getAirportIcao(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getStarIdent(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getCustomerIdent(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getProcessingCycle(),
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getDataSupplier(),
						toInteger(plTldStarSegment.getRow(system.getCursorRecordIndex()).getCreateDcrNumber()));
				vExist = rec.getInt();

			}
			if (vExist > 0) {

				return false;

			}

			log.info("checkSegmentPk Executed Successfully");
			return true;
		} catch (Exception e) {
			log.error("Error while executing checkSegmentPk" + e.getMessage());
			throw e;

		}
	}

	//
	@Override
	public void setWaypointCode() throws Exception {
		log.info("setWaypointCode Executing");
		// String query = "";
		// Record rec = null;
		try {
			String vWaypointCode = null;

			if (Objects.equals(parameter.getRecordType(), "S")) {
				vWaypointCode = rtrim(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWc1(), " ")
						+ nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWc2(), " ")
						+ nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWc3(), " ")
						+ nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWc4(), " "));
				if (!Objects.equals(nvl(vWaypointCode, " "),
						nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), " "))) {
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setWaypointDescCode(vWaypointCode);

				}

			}

			else {
				vWaypointCode = rtrim(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWc1(), " ")
						+ nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWc2(), " ")
						+ nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWc3(), " ")
						+ nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWc4(), " "));
				if (!Objects.equals(nvl(vWaypointCode, " "),
						nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), " "))) {
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setWaypointDescCode(vWaypointCode);

				}

			}

			log.info("setWaypointCode Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing setWaypointCode" + e.getMessage());
			throw e;

		}
	}

	//
	//
	@Override
	public void checkToCommit(String pActionType) throws Exception {
		log.info("checkToCommit Executing");
		// String query = "";
		// Record rec = null;
		try {
			String vMaster = substr(system.getCursorBlock(), 1, 11);
			Integer vButton = 1;
			Integer totalRows = 0;
			String cursorBlock = system.getCursorBlock();
			Integer cursorrecord = system.getCursorRecordIndex();
			// String cursorRecord = system.getCursorRecord();
			// String cursorItem = system.getCursorItem();
			String vTemp = null;
			String vButtonText = null;
			String vIgnoreRef = null;
			// Integer msgnum = messageCode;
			String lsTable = null;
			Integer lnCnt = 0;

			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				lsTable = toChar(nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".query_data_source_name"));

				// TODO set_waypoint_code --- Program Unit Calling
				setWaypointCode();
				if (Objects.equals(system.getFormStatus(), "CHANGED")
						|| (Objects.equals(controlBlock.getValidated(), "N") && !Objects.equals(nameIn(this,
								"control_block." + substr(system.getCursorBlock(), 4, 3) + "_VALIDATION_ERRORS"),
								null))) {
					if (Objects.equals(pActionType, "COMMIT")) {
						vButtonText = "Cancel";

						// TODO call_trans_ident_chk --- Program Unit Calling
						callTransIdentChk();
						parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
								global.getDataSupplier(), toInteger(global.getProcessingCycle()),
								toInteger(global.getDcrNumber()), parameter.getRecordType(), "SAVE"));

					}

					else if (Objects.equals(pActionType, "EXIT")) {
						vButtonText = "Exit Without Save";

					}

					else if (!Arrays.asList("ENTER_QUERY", "EXECUTE_QUERY").contains(pActionType)) {
						vButtonText = "Clear Without Save";

					}

					else {
						vButtonText = "Cancel Modification";

					}
					if (Objects.equals(global.getLibRefreshed(), "Y")) {
						alertDetails.getCurrent();
						if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
							vButton = moreButtons("C", "Refresh Record",
									"You have modified record(s). Select an option: \n \n"
											+ "1. Save and refresh Master Library\n"
											+ "2. Cancel modification, NO Save, NO Refresh",
									"Save&Refresh", vButtonText, "");
							OracleHelpers.bulkClassMapper(displayAlert, this);
							alertDetails.createNewRecord("checkToCommit1");
							throw new AlertException(event, alertDetails);
						} else {
							vButton = alertDetails.getAlertValue("checkToCommit1", alertDetails.getCurrentAlert());
						}

						if (vButton == 2 && Objects.equals(vButtonText, "Cancel")) {

							throw new FormTriggerFailureException(event);

						}

					}

					else {
						if (Objects.equals(pActionType, "COMMIT")) {
							vButton = 1;

						}

						else if (!Objects.equals(system.getFormStatus(), "CHANGED")
								&& Objects.equals(pActionType, "EXIT")) {
							vButton = 2;

						}

						else {
							alertDetails.getCurrent();
							if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
								vButton = moreButtons("C", "Refresh Record",
										"Record is modified or inserted or deleted. Select an\noption: ",
										"             Save             ", "    " + vButtonText + "    ", "");
								OracleHelpers.bulkClassMapper(displayAlert, this);
								alertDetails.createNewRecord("checkToCommit1");
								throw new AlertException(event, alertDetails);
							} else {
								vButton = alertDetails.getAlertValue("checkToCommit1", alertDetails.getCurrentAlert());
							}
						}
						if (Objects.equals(vButton, 1)) {
							parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
									global.getDataSupplier(), toInteger(global.getProcessingCycle()),
									toInteger(global.getDcrNumber()), parameter.getRecordType(), "SAVE"));
							cycledata();
							;
							commitForm(this);
							message("Record has been saved successfully");

							system.setFormStatus("NORMAL");

						}

						else {

							// throw new ExitFormException("noCommit");
							exitForm();

							// TODO EXIT_FORM(no_commit);

						}

					}
					if ((Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 2))
							|| (!Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 1))
							|| (!Objects.equals(global.getLibRefreshed(), "Y") && Objects.equals(vButton, 2))
							|| Objects.equals(vButton, 3)) {
						if (Objects.equals(pActionType, "COMMIT")) {

							// TODO call_trans_ident_chk --- Program Unit Calling
							callTransIdentChk();

						}

						else if (Objects.equals(pActionType, "EXIT")) {

							system.setFormStatus("CHANGED");
							exitForm();
//							throw new ExitFormException("noCommit");
							throw new FormTriggerFailureException();
						}

						else {
							this.plTldStar = new PlTldStar();
							this.plStdStar = new PlStdStar();

							this.plTldStarLeg = new Block<>();
							this.plTldStarSegment = new Block<>();
							this.plStdStarLeg = new Block<>();
							this.plStdStarSegment = new Block<>();
							system.setFormStatus("QUERY");

						}

					}

					else {
						if (!Objects.equals(rtrim(toString(nameIn(this, vMaster + ".airport_ident"))), null)) {
							if (!Objects.equals(pActionType, null)) {
								vIgnoreRef = "N";

							}

							else {
								vIgnoreRef = "Y";

							}

							// TODO validate_procedure(v_ignore_ref) --- Program Unit Calling
							this.plStdStarSegment.filterNonDeletedRecords();
							this.plTldStarSegment.filterNonDeletedRecords();
							this.plStdStarLeg.filterNonDeletedRecords();
							this.plTldStarLeg.filterNonDeletedRecords();
							validateProcedure(vIgnoreRef);

							// TODO call_trans_ident_chk --- Program Unit Calling
							callTransIdentChk();
							parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
									global.getDataSupplier(), toInteger(global.getProcessingCycle()),
									toInteger(global.getDcrNumber()), parameter.getRecordType(), "SAVE"));

							// TODO COMMIT_FORM;
							cycledata();
							mergeDelete();
							commitForm(this);
							message("Record has been saved successfully");
							system.setFormStatus("NORMAL");

							// TODO validate_procedure(v_ignore_ref) --- Program Unit Calling
							validateProcedure(vIgnoreRef);
							if (Objects.equals(parameter.getRecordType(), "T")) {
								vTemp = controlBlock.getTldValidationErrors();

							}

							else {
								vTemp = controlBlock.getStdValidationErrors();

							}

							Map<String, Object> dbcall = app.executeProcedure("CPTS", "check_valid_ind_const_prc",
									"Forms_Utilities",
									new ProcedureInParameter("pi_table", substr(lsTable, 4), OracleTypes.VARCHAR),
									new ProcedureInParameter("pi_dcr",
											toInteger(rtrim(toString(nameIn(this, vMaster + ".create_dcr_number")))),
											OracleTypes.NUMBER),
									new ProcedureInParameter("pi_supplier", global.getDataSupplier(),
											OracleTypes.VARCHAR),
									new ProcedureInParameter("pi_cycle",
											toInteger(rtrim(toString(nameIn(this, vMaster + ".processing_cycle")))),
											OracleTypes.NUMBER),
									new ProcedureOutParameter("po_count", OracleTypes.NUMBER));
							lnCnt = dbcall.get("po_count") == null ? null : toInteger(dbcall.get("po_count"));

							// forms_utilities.check_valid_ind_const_prc(substr(ls_Table,4),RTRIM(NAME_IN(v_master||".create_dcr_number")),global.getDataSupplier(),RTRIM(NAME_IN(v_master||".processing_cycle")),ln_cnt);
							if (lnCnt > 0) {
								if (Objects.equals(parameter.getRecordType(), "T")) {
									plTldStar.setValidateInd("I");
									if (Objects.equals(plTldStar.getRecordStatus(), "QUERIED"))
										plTldStar.setRecordStatus("CHANGED");

								}

								else {
									plStdStar.setValidateInd("I");
									if (Objects.equals(plStdStar.getRecordStatus(), "QUERIED"))
										plStdStar.setRecordStatus("CHANGED");

								}
								vTemp = vTemp
										+ "- There exists an inconsistency in validate indicator of STAR and STAR Leg. Please re-query the record to get proper error details.";

								// TODO
								// refresh_master_library.set_record_group(RTRIM(NAME_IN(v_master||".create_dcr_number")),RTRIM(NAME_IN(v_master||".validate_ind")),v_master,RTRIM(NAME_IN(v_master||".processing_cycle")),"U");
								this.refreshMasterLibrary.setRecordGroup(
										toInteger(rtrim(toString(nameIn(this, vMaster + ".create_dcr_number")))),
										rtrim(toString(nameIn(this, vMaster + ".validate_ind"))), vMaster,
										toInteger(rtrim(toString(nameIn(this, vMaster + ".processing_cycle")))), "U");

								// TODO commit_form;
								cycledata();
								commitForm(this);
								system.setFormStatus("NORMAL");
								message("Record has been saved successfully");

							}

							// TODO update_proc_type_and_altitude(v_master,'A') --- Program Unit Calling
							updateProcTypeAndAltitude(vMaster, "A");
							if (Objects.equals(system.getFormStatus(), "CHANGED")) {
								system.setCursorRecordIndex(cursorrecord);
								parameter.setUpdRec(
										coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
												global.getDataSupplier(), toInteger(global.getProcessingCycle()),
												toInteger(global.getDcrNumber()), parameter.getRecordType(), "SAVE"));

								// TODO call_trans_ident_chk --- Program Unit Calling
								callTransIdentChk();

								// TODO COMMIT_FORM;
								cycledata();
								commitForm(this);
								String _rowid = toString(
										nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".rowid"));
								sendUpdatedRowIdDetails(_rowid);
								system.setFormStatus("NORMAL");
								message("Record has been saved successfully");

							}

							if (!like("%STAR", cursorBlock)) {

								// TODO GO_RECORD(cursor_record);
								goRecord(cursorBlock, system.getCursorRecordIndex());

							}

							// goItem(cursorItem);

						}

						if (Arrays.asList("DOWN", "NEXT_RECORD").contains(pActionType)) {
							nextRecord("");

						}
						if (Objects.equals(pActionType, "EXIT")) {

							setApplicationProperty("cursorStyle", "default");
							exitForm();
						}

						if (Objects.equals(global.getLibRefreshed(), "Y")) {
							totalRows = getGroupRowCount(findGroup("Refresh_Records_Group"));
							if (totalRows > 0) {

								refreshMasterLibrary.refreshRecords(totalRows);
								// if((Objects.equals(msgnum, 40400))) {
								//
								// //TODO CLEAR_MESSAGE;
								//
								// }

							}

						}

						controlBlock.setValidated("Y");
						if (!OracleHelpers.isNullorEmpty(vTemp)) {
							if (Objects.equals(parameter.getRecordType(), "S")) {
								controlBlock.setStdValidationErrors(vTemp);
								setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
										FormConstant.PROPERTY_TRUE);
								setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
										FormConstant.PROPERTY_TRUE);

							}

							else {
								controlBlock.setTldValidationErrors(vTemp);
								setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
										FormConstant.PROPERTY_TRUE);
								setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
										FormConstant.PROPERTY_TRUE);

							}

						}

					}

				}

				else {
					refreshMasterLibrary.ifRefresh();
					if (pActionType.equals("EXIT")) {
						exitForm();
					}

				}

			}
			if (pActionType.equals("EXIT") && Objects.equals(parameter.getWorkType(), "VIEW")) {
				exitForm();
			}

			// coverity-fixes
			// if (Objects.equals(parameter.getRecordType(), "T")) {
			// // goItem("pl_tld_star.customer_ident");
			//
			// }
			//
			// else {
			// // goItem("pl_std_star.airport_ident");
			//
			// }

			log.info("checkToCommit Executed Successfully");
		} catch (Exception e) {
			if (e instanceof DataIntegrityViolationException || e instanceof NonUniqueObjectException
					|| e instanceof EntityExistsException || e instanceof ConstraintViolationException
					|| e instanceof org.hibernate.exception.ConstraintViolationException) {
				coreptLib.dspMsg("ORACLE error: unable to INSERT record. \n \n Please check the exact "
						+ "error message from the \"Dispaly \n Error\" in the \"HELP\" menu");
				log.info(" Unique Constrain Error while Executing the keyCommit Service");
				throw new FormTriggerFailureException(event);
			}

			log.error("Error while executing checkToCommit" + e.getMessage());
			throw e;

		}
	}

	public void dspError(String errMsg) throws Exception {
		log.info("dspError Executing");
		try {
			Object alertId = null;

			PropertyHelpers.setAlertProperty(event, "errorMessage", null, "alert_message_text", errMsg, null, "Ok", "",
					"");
			PropertyHelpers.setShowAlert(event, alertId + "_alert", false);
			log.info("dspError Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing dspError" + e.getMessage());
			throw e;

		}
	}

	//
	//// TODO PUnits Manual configuration
	// // ParentName ---> PLOT_GLOBAL_PKG
	//// File Name ---> corept_template.fmb
	//// TODO PUnits Manual configuration
	// // ParentName ---> PLOT_GLOBAL_PKG
	//// File Name ---> corept_template.fmb
	//
	@Override
	public void setUpdateDcr(String pBlock, Optional<CrAirportProcedure> optRecord) throws Exception {
		log.info("setUpdateDcr Executing");
		String query = "";
		// Record rec = null;
		try {
			CrAirportProcedure pRecord = new CrAirportProcedure();

			List<Record> getRecCur = null;
			List<Record> getRecCur1 = null;
			PlTldStar rtldStar = null;
			PlStdStar rstdStar = null;
			PlTldStarSegment rtldStarSegment = new PlTldStarSegment();
			PlStdStarSegment rstdStarSegment = new PlStdStarSegment();
			PlTldStarLeg rtldStarLeg = new PlTldStarLeg();
			PlStdStarLeg rstdStarLeg = new PlStdStarLeg();
			Integer vDcr = toInteger(nameIn(this, pBlock + ".create_dcr_number"));
			String vCycleData = substr(toString(nameIn(this, pBlock + ".processing_cycle")), 3);
			Integer vCount = 0;
			if (optRecord != null && optRecord.isPresent()) {
				pRecord = optRecord.get();
			}

			if (rstdStarLeg != null) {
				log.info(" " + rstdStarLeg);
			}

			if (like("PL_STD_STAR", pBlock)) {
				query = " SELECT * FROM pl_std_star WHERE create_dcr_number=?";
				getRecCur = app.executeQuery(query, vDcr);
				for (Record stdStar : getRecCur) {

					rstdStar = app.mapResultSetToClass(stdStar, PlStdStar.class);

					// TODO FETCHget_rec_curINTOrstd_star
					vCount = vCount + 1;
					if (!Objects.equals(nvl(plStdStar.getAreaCode(), " "), nvl(rstdStar.getAreaCode(), " "))
							|| !Objects.equals(
									nvl(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType(), " "),
									nvl(rstdStarSegment.getAircraftType(), " "))
							|| !Objects.equals(nvl(plStdStar.getSpecialsInd(), " "),
									nvl(rstdStar.getSpecialsInd(), " "))
							|| !Objects.equals(nvl(plStdStar.getSaaarStarInd(), " "),
									nvl(rstdStar.getSaaarStarInd(), " "))) {
						copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

					}

				}
				// TODO CLOSEget_rec_cur

			}

			else if (like("PL_TLD_STAR", pBlock)) {
				query = " SELECT * FROM pl_tld_star WHERE create_dcr_number=?";
				getRecCur1 = app.executeQuery(query, vDcr);
				for (Record tldStar : getRecCur1) {
					rtldStar = app.mapResultSetToClass(tldStar, PlTldStar.class);

					vCount = vCount + 1;
					if (!Objects.equals(
							nvl(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType(), " "),
							nvl(rtldStarSegment, " "))
							|| !Objects.equals(nvl(plTldStar.getSpecialsInd(), " "),
									nvl(rtldStar.getSpecialsInd(), " "))
							|| !Objects.equals(nvl(plTldStar.getSaaarStarInd(), " "),
									nvl(rtldStar.getSaaarStarInd(), " "))) {
						copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

					}

				}

			}

			else if (like("PL_STD_STAR_SEGMENT", pBlock)) {
				query = " SELECT * FROM pl_std_star_segment WHERE create_dcr_number=?";
				getRecCur = app.executeQuery(query, vDcr);
				for (Record stdStarSegment : getRecCur) {
					rstdStarSegment = app.mapResultSetToClass(stdStarSegment, PlStdStarSegment.class);
					vCount = vCount + 1;
					if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
							rstdStarSegment.getRouteType())
							|| !Objects.equals(
									plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
									rstdStarSegment.getTransitionIdent())
							|| !Objects.equals(
									nvl(plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1(), " "),
									nvl(rstdStarSegment.getQualifier1(), " "))
							|| !Objects.equals(
									nvl(plStdStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2(), " "),
									nvl(rstdStarSegment.getQualifier2(), " "))
							|| !Objects.equals(
									nvl(plStdStarSegment.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd(),
											" "),
									nvl(rstdStarSegment.getProcDesignMagVarInd(), " "))) {
						copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_SEGMENT") + ".update_dcr_number");
						copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");
						query = " SELECT * FROM pl_std_star_leg WHERE airport_ident = ? AND airport_icao=? AND star_ident=? AND processing_cycle=? AND transition_ident=? AND data_supplier=? AND route_type=? ORDER BY sequence_num";
						getRecCur1 = app.executeQuery(query, rstdStarSegment.getAirportIdent(),
								rstdStarSegment.getAirportIcao(), rstdStarSegment.getStarIdent(),
								rstdStarSegment.getProcessingCycle(), rstdStarSegment.getTransitionIdent(),
								rstdStarSegment.getDataSupplier(), rstdStarSegment.getRouteType());
						int i = 0;
						for (Record stdStarLeg : getRecCur1)// TODO se9uence_num;
						{
							if (stdStarLeg != null) {
								log.info(" " + stdStarLeg);
							}
							rstdStarLeg = app.mapResultSetToClass(stdStarSegment, PlStdStarLeg.class);

							system.setCursorRecordIndex(i);
							if (!Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(),
									null)) {
								for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
									if (Objects.equals(plStdStarLeg.getCreateDcrNumber(),
											rstdStarLeg.getCreateDcrNumber())) {
										if (!Objects.equals(
												plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
												rstdStarSegment.getRouteType())
												|| !Objects.equals(
														plStdStarSegment.getRow(system.getCursorRecordIndex())
																.getTransitionIdent(),
														rstdStarSegment.getTransitionIdent())) {
											copy(global.getDcrNumber(), "pl_std_star_leg.update_dcr_number");

										}

										copy(substr(toString(plStdStar.getProcessingCycle()), 3),
												"pl_std_star_leg.cycle_data");

									}

								}

							}
							i++;
						}

					}

				}

			}

			else if (like("PL_TLD_STAR_SEGMENT", pBlock)) {
				query = "SELECT * FROM pl_tld_star_segment WHERE create_dcr_number=?";
				getRecCur = app.executeQuery(query, vDcr);
				for (Record tldStarSegment : getRecCur) {
					rtldStarSegment = app.mapResultSetToClass(tldStarSegment, PlTldStarSegment.class);

					vCount = vCount + 1;
					if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
							rtldStarSegment.getRouteType())
							|| !Objects.equals(
									plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
									rtldStarSegment.getTransitionIdent())
							|| !Objects.equals(
									nvl(plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier1(), " "),
									nvl(rtldStarSegment.getQualifier1(), " "))
							|| !Objects.equals(
									nvl(plTldStarSegment.getRow(system.getCursorRecordIndex()).getQualifier2(), " "),
									nvl(rtldStarSegment.getQualifier2(), " "))
							|| !Objects.equals(
									nvl(plTldStarSegment.getRow(system.getCursorRecordIndex()).getProcDesignMagVarInd(),
											" "),
									nvl(rtldStarSegment.getProcDesignMagVarInd(), " "))) {
						copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_SEGMENT") + ".update_dcr_number");
						copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

						query = " SELECT * FROM pl_tld_star_leg WHERE customer_ident=? AND airport_ident=? AND airport_icao=? AND star_ident=? AND processing_cycle=? AND transition_ident=? AND data_supplier=? AND route_type=? ORDER BY sequence_num";
						getRecCur1 = app.executeQuery(query, rtldStarSegment.getCustomerIdent(),
								rtldStarSegment.getAirportIdent(), rtldStarSegment.getAirportIcao(),
								rtldStarSegment.getStarIdent(), rtldStarSegment.getProcessingCycle(),
								rtldStarSegment.getTransitionIdent(), rtldStarSegment.getDataSupplier(),
								rtldStarSegment.getRouteType());
						int i = 0;
						for (Record rtldStarleg : getRecCur1)// TODO se9uence_num;
						{
							system.setCursorRecordIndex(i);

							if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(),
									null)) {
								for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
									if (Objects.equals(plTldStarLeg.getCreateDcrNumber(),
											rtldStarLeg.getCreateDcrNumber())) {
										if (!Objects.equals(
												plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
												rtldStarSegment.getRouteType())
												|| !Objects.equals(
														plTldStarSegment.getRow(system.getCursorRecordIndex())
																.getTransitionIdent(),
														rtldStarSegment.getTransitionIdent())) {
											copy(toInteger(global.getDcrNumber()), "pl_tld_star_leg.update_dcr_number");

										}

										copy(substr(toString(plTldStar.getProcessingCycle()), 3),
												"pl_tld_star_leg.cycle_data");

									}

								}

							}
							i++;

						}

					}

				}

			}

			else if (like("PL_STD_STAR_LEG", pBlock)) {
				query = "SELECT * FROM pl_std_star_leg WHERE create_dcr_number=?";
				getRecCur = app.executeQuery(query, vDcr);
				for (Record stdStarLeg : getRecCur) {
					rstdStarLeg = app.mapResultSetToClass(stdStarLeg, PlStdStarLeg.class);

					vCount = vCount + 1;
					if (!Objects.equals(pRecord.getAlt1(), nvl(rstdStarLeg.getAlt1(), " "))
							|| !Objects.equals(pRecord.getAlt2(), nvl(rstdStarLeg.getAlt2(), " "))
							|| !Objects.equals(pRecord.getAltDescription(), nvl(rstdStarLeg.getAltDescription(), " "))
							|| !Objects.equals(
									nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getArcRadius(), 0),
									nvl(rstdStarLeg.getArcRadius(), 0))
							|| !Objects.equals(pRecord.getAtcInd(), nvl(rstdStarLeg.getAtcInd(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixIcaoCode(), " "),
									nvl(rstdStarLeg.getCenterFixIcaoCode(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixIdent(), " "),
									nvl(rstdStarLeg.getCenterFixIdent(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixMultipleCode(), " "),
									nvl(rstdStarLeg.getCenterFixMultipleCode(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixSection(), " "),
									nvl(rstdStarLeg.getCenterFixSection(), " "))
							|| !Objects.equals(pRecord.getCenterFixSubsection(),
									nvl(rstdStarLeg.getCenterFixSubsection(), " "))
							|| !Objects.equals(nvl(pRecord.getFixIcaoCode(), " "),
									nvl(rstdStarLeg.getFixIcaoCode(), " "))
							|| !Objects.equals(nvl(pRecord.getFixIdent(), " "), nvl(rstdStarLeg.getFixIdent(), " "))
							|| !Objects.equals(nvl(pRecord.getFixSectionCode(), " "),
									nvl(rstdStarLeg.getFixSectionCode(), " "))
							|| !Objects.equals(pRecord.getFixSubsectionCode(),
									nvl(rstdStarLeg.getFixSubsectionCode(), " "))
							|| !Objects.equals(pRecord.getMagneticCourse(),
									rpad(nvl(rstdStarLeg.getMagneticCourse(), " "), 4))
							|| !Objects.equals(pRecord.getPathAndTermination(),
									nvl(rstdStarLeg.getPathAndTermination(), "  "))
							|| !Objects.equals(nvl(pRecord.getRecommNavaidIcaoCode(), " "),
									nvl(rstdStarLeg.getRecommNavaidIcaoCode(), " "))
							|| !Objects.equals(nvl(pRecord.getRecommNavaidIdent(), " "),
									nvl(rstdStarLeg.getRecommNavaidIdent(), " "))
							|| !Objects.equals(nvl(pRecord.getRecommNavaidSection(), " "),
									nvl(rstdStarLeg.getRecommNavaidSection(), " "))
							|| !Objects.equals(pRecord.getRecommNavaidSubsection(),
									nvl(rstdStarLeg.getRecommNavaidSubsection(), " "))
							|| !Objects.equals(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRho(), 0),
									nvl(rstdStarLeg.getRho(), 0))
							|| !Objects.equals(nvl(pRecord.getRnp(), " "), nvl(rstdStarLeg.getRnp(), " "))
							|| !Objects.equals(nvl(pRecord.getRouteDistance(), " "),
									nvl(rstdStarLeg.getRouteDistance(), " "))
							|| !Objects.equals(
									nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSpeedLimit(), 0),
									nvl(rstdStarLeg.getSpeedLimit(), 0))
							|| !Objects.equals(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTheta(), 0),
									nvl(rstdStarLeg.getTheta(), 0))
							|| !Objects.equals(
									nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransAltitude(), 0),
									nvl(rstdStarLeg.getTransAltitude(), 0))
							|| !Objects.equals(pRecord.getTurnDirValid(), nvl(rstdStarLeg.getTurnDirValid(), " "))
							|| !Objects.equals(pRecord.getTurnDirection(), nvl(rstdStarLeg.getTurnDirection(), " "))
							|| !Objects.equals(
									nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), 0),
									nvl(rstdStarLeg.getVerticalAngle(), 0))
							|| !Objects.equals(pRecord.getWaypointDescCode(),
									nvl(rpad(rstdStarLeg.getWaypointDescCode(), 4, " "), "    "))
							|| !Objects.equals(
									nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getProcDesignMagVar(), " "),
									nvl(rstdStarLeg.getProcDesignMagVar(), " "))) {
						copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_LEG") + ".update_dcr_number");
						copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");
						copy(substr(toString(plStdStar.getProcessingCycle()), 3), pBlock + ".cycle_data");

					}

				}
				// TODO CLOSEget_rec_cur

			}

			else if (like("PL_TLD_STAR_LEG", pBlock)) {
				query = " SELECT * FROM pl_tld_star_leg WHERE create_dcr_number=?";
				getRecCur = app.executeQuery(query, vDcr);
				for (Record tldStarLeg : getRecCur) {
					rtldStarLeg = app.mapResultSetToClass(tldStarLeg, PlTldStarLeg.class);

					vCount = vCount + 1;
					if (!Objects.equals(pRecord.getAlt1(), nvl(rtldStarLeg.getAlt1(), " "))
							|| !Objects.equals(pRecord.getAlt2(), nvl(rtldStarLeg.getAlt2(), " "))
							|| !Objects.equals(pRecord.getAltDescription(), nvl(rtldStarLeg.getAltDescription(), " "))
							|| !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getArcRadius(), 0),
									nvl(rtldStarLeg.getArcRadius(), 0))
							|| !Objects.equals(pRecord.getAtcInd(), nvl(rtldStarLeg.getAtcInd(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixIcaoCode(), " "),
									nvl(rtldStarLeg.getCenterFixIcaoCode(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixIdent(), " "),
									nvl(rtldStarLeg.getCenterFixIdent(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixMultipleCode(), " "),
									nvl(rtldStarLeg.getCenterFixMultipleCode(), " "))
							|| !Objects.equals(nvl(pRecord.getCenterFixSection(), " "),
									nvl(rtldStarLeg.getCenterFixSection(), " "))
							|| !Objects.equals(pRecord.getCenterFixSubsection(),
									nvl(rtldStarLeg.getCenterFixSubsection(), " "))
							|| !Objects.equals(nvl(pRecord.getFixIcaoCode(), " "),
									nvl(rtldStarLeg.getFixIcaoCode(), " "))
							|| !Objects.equals(nvl(pRecord.getFixIdent(), " "), nvl(rtldStarLeg.getFixIdent(), " "))
							|| !Objects.equals(nvl(pRecord.getFixSectionCode(), " "),
									nvl(rtldStarLeg.getFixSectionCode(), " "))
							|| !Objects.equals(pRecord.getFixSubsectionCode(),
									nvl(rtldStarLeg.getFixSubsectionCode(), " "))
							|| !Objects.equals(pRecord.getMagneticCourse(),
									rpad(nvl(rtldStarLeg.getMagneticCourse(), " "), 4))
							|| !Objects.equals(pRecord.getPathAndTermination(),
									nvl(rtldStarLeg.getPathAndTermination(), "  "))
							|| !Objects.equals(nvl(pRecord.getRecommNavaidIcaoCode(), " "),
									nvl(rtldStarLeg.getRecommNavaidIcaoCode(), " "))
							|| !Objects.equals(nvl(pRecord.getRecommNavaidIdent(), " "),
									nvl(rtldStarLeg.getRecommNavaidIdent(), " "))
							|| !Objects.equals(nvl(pRecord.getRecommNavaidSection(), " "),
									nvl(rtldStarLeg.getRecommNavaidSection(), " "))
							|| !Objects.equals(pRecord.getRecommNavaidSubsection(),
									nvl(rtldStarLeg.getRecommNavaidSubsection(), " "))
							|| !Objects.equals(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRho(), 0),
									nvl(rtldStarLeg.getRho(), 0))
							|| !Objects.equals(nvl(pRecord.getRnp(), " "), nvl(rtldStarLeg.getRnp(), " "))
							|| !Objects.equals(nvl(pRecord.getRouteDistance(), " "),
									nvl(rtldStarLeg.getRouteDistance(), " "))
							|| !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSpeedLimit(), 0),
									nvl(rtldStarLeg.getSpeedLimit(), 0))
							|| !Objects.equals(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTheta(), 0),
									nvl(rtldStarLeg.getTheta(), 0))
							|| !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransAltitude(), 0),
									nvl(rtldStarLeg.getTransAltitude(), 0))
							|| !Objects.equals(pRecord.getTurnDirValid(), nvl(rtldStarLeg.getTurnDirValid(), " "))
							|| !Objects.equals(pRecord.getTurnDirection(), nvl(rtldStarLeg.getTurnDirection(), " "))
							|| !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getVerticalAngle(), 0),
									nvl(rtldStarLeg.getVerticalAngle(), 0))
							|| !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), " "),
									nvl(rtldStarLeg.getWaypointDescCode(), " "))
							|| !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getProcDesignMagVar(), " "),
									nvl(rtldStarLeg.getProcDesignMagVar(), " "))) {
						copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_LEG") + ".update_dcr_number");
						copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");
						copy(substr(toString(plTldStar.getProcessingCycle()), 3), pBlock + ".cycle_data");

					}

				}
				// TODO CLOSEget_rec_cur

			}

			if (Objects.equals(vCount, 0)) {
				if (like("%LEG", pBlock)) {
					copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_LEG") + ".update_dcr_number");
					copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

				}

				else if (like("%SEGMENT", pBlock)) {
					copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_SEGMENT") + ".update_dcr_number");
					copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

				}

				else {
					copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

				}
				if (like("%LEG", pBlock)) {
					copy(vCycleData, pBlock + ".cycle_data");

				}

			}

			else if (Objects.equals(parameter.getUpdDcrDel(), "Y")) {
				if (like("%LEG", pBlock)) {
					copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_LEG") + ".update_dcr_number");

				}

				else if (like("%SEGMENT", pBlock)) {
					copy(toInteger(global.getDcrNumber()), rtrim(pBlock, "_SEGMENT") + ".update_dcr_number");

				}

				else {
					copy(toInteger(global.getDcrNumber()), pBlock + ".update_dcr_number");

				}
				parameter.setUpdDcrDel("N");

			}

			log.info("setUpdateDcr Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing setUpdateDcr" + e.getMessage());
			throw e;

		}
	}

	//
	@Override
	public void callTransIdentChk() throws Exception {
		log.info("callTransIdentChk Executing");
		// String query = "";
		// Record rec = null;
		try {

			// TODO chk_segment_trans_ident --- Program Unit Calling
			chkSegmentTransIdent();

			// TODO chk_leg_trans_ident --- Program Unit Calling
			chkLegTransIdent();

			log.info("callTransIdentChk Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing callTransIdentChk" + e.getMessage());
			throw e;

		}
	}

	//
	//
	@Override
	public void chkLegTransIdent() throws Exception {
		log.info("chkLegTransIdent Executing");

		try {
			Integer vNum = 0;
			String vBlock = null;
			int size = 0;

			if (Objects.equals(parameter.getRecordType(), "T")) {
				vBlock = "PL_TLD_STAR_LEG";
				size = plTldStarLeg.size();

			}

			else {
				vBlock = "PL_STD_STAR_LEG";
				size = plStdStarLeg.size();

			}

			// TODO first_record;
			for (int i = 0; i < size; i++) {

				BigDecimal bd = app.executeFunction(BigDecimal.class, "CPTS", "Check_trans_ident_ID", "forms_utilities",
						OracleTypes.NUMBER, new ProcedureInParameter("p_trans",
								toChar(nameIn(this, vBlock + ".TRANSITION_IDENT")), OracleTypes.VARCHAR));

				vNum = bd == null ? null : bd.intValue();

				if (Objects.equals(vNum, 0)) {
					coreptLib.dspMsg("Transition Ident " + nameIn(this, vBlock + ".TRANSITION_IDENT")
							+ "is Invalid for Procedure." + chr(10) + "It contains ' - ' along with other characters.");

					throw new FormTriggerFailureException();

				}

			}

			log.info("chkLegTransIdent Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing chkLegTransIdent" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void chkSegmentTransIdent() throws Exception {
		log.info("chkSegmentTransIdent Executing");

		try {
			Integer vNum = 0;
			String vBlock = null;
			int size = 0;

			if (Objects.equals(parameter.getRecordType(), "T")) {
				vBlock = "PL_TLD_STAR_SEGMENT";
				size = plTldStarSegment.size();

			}

			else {
				vBlock = "PL_STD_STAR_SEGMENT";
				size = plStdStarSegment.size();

			}

			for (int i = 0; i < size; i++) {
				BigDecimal bd = app.executeFunction(BigDecimal.class, "CPTS", "Check_trans_ident_ID", "forms_utilities",
						OracleTypes.NUMBER, new ProcedureInParameter("p_trans",
								toChar(nameIn(this, vBlock + ".TRANSITION_IDENT")), OracleTypes.VARCHAR));

				vNum = bd == null ? null : bd.intValue();

				if (Objects.equals(vNum, 0)) {
					coreptLib.dspMsg("Transition Ident " + nameIn(this, vBlock + ".TRANSITION_IDENT")
							+ "is Invalid for Procedure." + chr(10) + "It contains ' - ' along with other characters.");

					throw new FormTriggerFailureException();

				}

			}

			log.info("chkSegmentTransIdent Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing chkSegmentTransIdent" + e.getMessage());
			throw e;

		}
	}

	@Override
	public String validateRwyTransition() throws Exception {
		log.info("validateRwyTransition Executing");
		// String query = "";
		// Record rec = null;
		try {
			String vAllErr = null;
			String vCvalidind = null;
			String vStable = null;
			CrAirportProcedure vRrecord = new CrAirportProcedure();
			Integer vNcycle = 0;
			Integer vNprocessingCycle = 0;

			// TODO populate_record(v_rRecord,v_nCycle,v_nProcessing_Cycle) --- Program Unit
			// Calling
			populateRecord(vRrecord, vNcycle, vNprocessingCycle);
			vStable = "STAR";
			if (toInteger(global.getRecentCycle()) >= vNprocessingCycle) {
				vNcycle = vNprocessingCycle;

			}

			else {
				vNcycle = toInteger(global.getRecentCycle());

			}
			Map<String, Object> dbCall = app.executeProcedure("recsv2", "chk_transition_ident", null,
					new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
					new ProcedureInParameter("p_proc_cycle", vNcycle, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_crecord", vRrecord, OracleTypes.STRUCT,
							" sdv_records.cr_airport_procedure"),
					new ProcedureInParameter("p_dcr_number", null, OracleTypes.NUMBER),
					new ProcedureInParameter("p_sln_master", null, OracleTypes.NUMBER),
					new ProcedureInParameter("p_table", vStable, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_err_no", null, OracleTypes.NUMBER),
					new ProcedureInParameter("p_input", 1, OracleTypes.NUMBER),
					new ProcedureOutParameter("p_valind", OracleTypes.VARCHAR));
			vCvalidind = toString(dbCall.get("p_valind"));
			// TODO
			// recsv2.chk_transition_ident(global.getDataSupplier(),v_nCycle,v_rRecord,null,null,v_sTable,null,1,v_cValidind);
			if (Objects.equals(vCvalidind, "I")) {

				vAllErr = getNullClean(vAllErr) + " * 487 - " + coreptLib.getErrText(487) + chr(10);

			}

			log.info("validateRwyTransition Executed Successfully");
			return (vAllErr);

		} catch (Exception e) {
			log.error("Error while executing validateRwyTransition" + e.getMessage());
			throw e;

		}
	}
	//

	@Override
	public void magneticCourseSdvPrc(String piRecordType) throws Exception {
		log.info("magneticCourseSdvPrc Executing");
		// String query = "";
		// Record rec = null;
		try {
			String lsBlock = null;
			log.debug("The default value of lsBlock is " + lsBlock);
			Object lsMagneticCourse = null;
			int size = 0;

			if (Objects.equals(piRecordType, "S")) {
				lsBlock = "PL_STD_STAR_LEG";
				size = plStdStarLeg.size();

			}

			else {
				lsBlock = "PL_TLD_STAR_LEG";
				size = plTldStarLeg.size();

			}

			// TODO FIRST_RECORD;
			for (int i = 0; i < size; i++) {
				lsMagneticCourse = nameIn(this, lsBlock + ".magnetic_course");
				if (!Objects.equals(lsMagneticCourse, null)) {

					app.executeProcedure("recsv2", "vset_global_constant", null,
							new ProcedureInParameter("p_nErrNum", 1166, OracleTypes.VARCHAR),
							new ProcedureInParameter("pi_Magnetic_Course", lsMagneticCourse, OracleTypes.VARCHAR));
					// TODO recsv2.vset_global_constant(1166,ls_Magnetic_Course);
					break;

				}

				// break;
				// nextRecord( "");

			}

			log.info("magneticCourseSdvPrc Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing magneticCourseSdvPrc" + e.getMessage());
			throw e;

		}
	}

	//
	@Override
	public Integer getCycle() throws Exception {
		log.info("getCycle Executing");
		// String query = "";
		// Record rec = null;
		try {

			if (toInteger(plTldStar.getProcessingCycle()) > toInteger(global.getMaxCycle())) {
				log.info("getCycle Executed Successfully");

				return toInteger(global.getMaxCycle());

			}

			else {
				log.info("getCycle Executed Successfully");

				return toInteger(nvl(plTldStar.getProcessingCycle(), global.getProcessingCycle()));

			}

		} catch (Exception e) {
			log.error("Error while executing getCycle" + e.getMessage());
			throw e;

		}
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// onMessage(AirportStarTriggerRequestDto reqDto) throws Exception{
	// log.info(" onMessage Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// Integer msgnum = messageCode;
	// String msgtxt = messageText;
	// String msgtyp = messageType;
	//
	//
	// //TODO Set_Application_Property(cursor_style,"DEFAULT");
	// if((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) ||
	// Objects.equals(msgnum, 40407))) {
	//
	// //TODO CLEAR_MESSAGE;
	// parameter.setUpdRec("N");
	// setBlockProperty(nameIn(this, "system.cursor_block"),
	// FormConstant.UPDATE_ALLOWED, FormConstant.PROPERTY_TRUE);
	// message("Record has been saved successfully");
	//
	// }
	//
	//
	// else if(Arrays.asList(41051,40350,47316,40353,40352).contains(msgnum)) {
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
	// else {
	//
	// //TODO display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
	// "||msgtxt);
	// throw new FormTriggerFailureException();
	//
	// }
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" onMessage executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the onMessage Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// onError(AirportStarTriggerRequestDto reqDto) throws Exception{
	// log.info(" onError Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// Integer msgnum = errorCode;
	// String msgtxt = errorText;
	// String msgtyp = errorType;
	// String vBlockName = system.getCursorBlock();
	//
	//
	// //TODO Set_Application_Property(cursor_style,"DEFAULT");
	// if((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) ||
	// Objects.equals(msgnum, 40407))) {
	// message("Changes saved successfully");
	//
	// }
	//
	//
	// else if(Arrays.asList(41051,40350,47316,40353,40352).contains(msgnum)) {
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
	// else if(Objects.equals(msgnum, 40100)) {
	//
	// //TODO clear_message;
	// message("at the first record.");
	//
	// }
	//
	//
	// else if(Objects.equals(msgnum, 40735) && like( "%01031%",msgtxt)) {
	//
	// //TODO clear_message;
	//
	// //TODO dsp_msg(msgtxt||" Insufficient privileges. ");
	//
	// }
	//
	//
	// else if(Objects.equals(msgnum, 40501)) {
	// controlBlock.setTempField("Y");
	//
	// }
	//
	//
	// else if(Arrays.asList(40508,40509).contains(msgnum)) {
	//
	// //TODO dsp_msg(msgtxt||chr(10)||chr(10)||"Please check the exact error
	// message from the "Display Error" in the "HELP" menu");
	//
	// }
	//
	//
	// else if(Arrays.asList(40200).contains(msgnum)) {
	// if(Objects.equals(parameter.getUpdRec(), "N")) {
	// if(!Objects.equals(parameter.getWorkType(), "VIEW")) {
	// if(Objects.equals(parameter.getRecordType(), "T")) {
	// if(Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
	//
	// //TODO dsp_msg("Record Cannot be Updated as
	// "||nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT")||" is not
	// associated with DCR# "||global.getDcrNumber()||" Or with processing Cycle
	// "||global.getProcessingCycle());
	//
	// }
	//
	//
	// else {
	//
	// //TODO dsp_msg("Record Cannot be Updated as
	// "||nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT")||" is not
	// associated with DCR# "||global.getDcrNumber()||" Or with processing Cycle
	// "||nameIn(this,v_block_name||".processing_cycle"));
	//
	// }
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	// else {
	// if(Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
	//
	// //TODO dsp_msg("STD Record Cannot be Updated with DCR#
	// "||global.getDcrNumber()||" Or with processing Cycle
	// "||global.getProcessingCycle());
	//
	// }
	//
	//
	// else {
	//
	// //TODO dsp_msg("STD Record Cannot be Updated with DCR#
	// "||global.getDcrNumber()||" Or with processing Cycle
	// "||nameIn(this,v_block_name||".processing_cycle"));
	//
	// }
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else {
	//
	// //TODO dsp_msg(msgtxt);
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else {
	//
	// //TODO dsp_msg(msgtxt);
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else if(Objects.equals(msgnum, 41050) &&
	// !Objects.equals(parameter.getWorkType(), "VIEW")) {
	// if(Objects.equals(parameter.getUpdRec(), "N")) {
	// null;
	//
	// }
	//
	//
	// else {
	//
	// //TODO dsp_msg(msgtxt);
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else {
	//
	// //TODO display_alert.one_button("S","Error",msgtyp||"-"||TO_CHAR(msgnum)||":
	// "||msgtxt);
	//
	// }
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" onError executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the onError Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// keyCrerec(AirportStarTriggerRequestDto reqDto) throws Exception{
	// log.info(" keyCrerec Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// if(Objects.equals(parameter.getWorkType(), "VIEW")) {
	// null;
	//
	// }
	//
	//
	// else {
	// //TODO parameter.setUpdRec(setActionRestr(nameIn(this,
	// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"CRE"));
	//
	// }
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" keyCrerec executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the keyCrerec Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }

	public void getIndexData() throws Exception {
		try {
			if (parameter.getLibraryAccess().startsWith("PRE")) {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					int i = 0;
					for (PlStdStarLeg leg : plStdStarLeg.getData()) {
						if (Objects.equals(global.getSearch(), leg.getSequenceNum())
								&& Objects.equals(global.getSearchTransIdent(), leg.getTransitionIdent())) {
							system.setCurrentRecordIndex(i);
						}
						i++;

					}

				} else {
					int i = 0;
					for (PlTldStarLeg leg : plTldStarLeg.getData()) {
						if (Objects.equals(global.getSearch(), leg.getSequenceNum())
								&& Objects.equals(global.getSearchTransIdent(), leg.getTransitionIdent())) {

							system.setCurrentRecordIndex(i);
						}
						i++;

					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}

	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> whenNewFormInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" whenNewFormInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String query = null;
			Record rec = null;

			// TODO Initialize_Form --- Program Unit Calling
			coreptTemplateTriggerServiceImpl.initializeForm();
			displayItemBlock.setFormName("AIRPORT STAR");
			// TODO set_block;
			coreptLib.setBlock();
			controlBlock.setPkSegmentChange("N");
			controlBlock.setPkAircraftChange("N");
			controlBlock.setPkRouteChange("N");
			controlBlock.setPkTransitionChange("N");
			controlBlock.setCountInvalid(0);

			// TODO if_from_error_summary;

			String fromErrorSummary = new String(global.getFromErrorSummary()),
					countRecords = new String(global.getCountRecords() == null ? "" : global.getCountRecords());
			coreptLib.iffromerrorsummary();
			if (Objects.equals(fromErrorSummary, "Y") && Objects.equals(countRecords, "Y"))
				queryMasterDetails();

			// TODO if_from_leg_search(1);
			if (Objects.equals(global.getOpenForm(), true)) {
				preKeyexe(reqDto);
				coreptLib.iffromlegsearch(1);
				if (parameter.getLibraryAccess().startsWith("PRE")) {
					if (Objects.equals(parameter.getRecordType(), "S")) {
						callPostquery("PL_STD_STAR");
					} else {
						callPostquery("PL_TLD_STAR");
					}
				}

				setrelation();
				if (!Objects.equals(global.getSearch(), null) && !Objects.equals(global.getSearchTransIdent(), null)) {
					getIndexData();
				}
				if (Objects.equals(parameter.getRecordType(), "S")) {
					system.setCursorRecordIndex(0);
					plStdStarWhenNewRecordInstance();
				} else {
					system.setCursorRecordIndex(0);
					plTldStarWhenNewRecordInstance();
				}
				global.setOpenForm(false);
			} else {
				coreptLib.iffromlegsearch(1);
			}
			global.setLibraryAccess(parameter.getLibraryAccess());
			global.setRecordType(parameter.getRecordType());

			query = """
					select max(processing_cycle)
					from   pl_std_airport
					where data_supplier = ?
					""";
			rec = app.selectInto(query, global.getDataSupplier());
			global.setMaxCycle(rec.getString());
			if (like("PRE%", parameter.getLibraryAccess())) {
				if (!system.getFormStatus().equals("NEW")) {
					global.setCreateDcrNumber(toString(
							nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
				}
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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> onClearDetails(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" onClearDetails Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapperV2(reqDto, this, true);

			String smas = "";
			String sseg = "";
			String sleg = "";
			String tmas = "";
			String tseg = "";
			String tleg = "";
			if (like("PL_STD_STAR", system.getCursorBlock())) {
				smas = plStdStar.getLastQuery();
				sseg = plStdStarSegment.getLastQuery();
				sleg = plStdStarLeg.getLastQuery();
			} else {
				tmas = plTldStar.getLastQuery();
				tseg = plTldStarSegment.getLastQuery();
				tleg = plTldStarLeg.getLastQuery();
			}
			if (Objects.equals(system.getFormStatus(), "CHANGED")
					&& !Arrays.asList("DELETE_RECORD", "COUNT_QUERY", "DOWN", "NEXT_RECORD")
							.contains(system.getCoordinationOperation())) {

				// TODO CHECK_TO_COMMIT(:system.COORDINATION_OPERATION) --- Program Unit Calling
				mergeDelete();
				checkToCommit(system.getCoordinationOperation());
				if (Objects.equals(system.getFormStatus(), "CHANGED")) {
					throw new FormTriggerFailureException(event);

				}

			}

			// TODO Clear_All_Master_Details --- Program Unit Calling
			// this.plTldStar = new PlTldStar();
			// this.plStdStar = new PlStdStar();
			this.plTldStarLeg = new Block<>();
			this.plTldStarSegment = new Block<>();
			this.plStdStarLeg = new Block<>();
			this.plStdStarSegment = new Block<>();
			if (Objects.equals(parameter.getRecordType(), "S")) {
				setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_FALSE);
				setItemProperty("control_block.std_leg_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);
				controlBlock.setStdOverrideErrors(null);
				setItemProperty("control_block.std_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

			}

			else {
				setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_FALSE);
				setItemProperty("control_block.tld_leg_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);
				controlBlock.setTldOverrideErrors(null);
				setItemProperty("control_block.tld_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

			}
			if (like("PL_STD_STAR", system.getCursorBlock())) {
				plStdStar.setLastQuery(smas);
				plStdStarSegment.setLastQuery(sseg);
				plStdStarLeg.setLastQuery(sleg);

			} else {
				plTldStar.setLastQuery(tmas);
				plTldStarSegment.setLastQuery(tseg);
				plTldStarLeg.setLastQuery(tleg);
			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" onClearDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the onClearDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);

			return ExceptionUtils.handleException(e, resDto);
		}
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// keyDown(AirportStarTriggerRequestDto reqDto) throws Exception{
	// log.info(" keyDown Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	//
	// //TODO pc3_do_key("NEXT_RECORD");
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" keyDown executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the keyDown Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyNxtrec(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyNxtrec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapper(reqDto, this);
			if (like("PL%STAR", system.getCursorBlock()) && Objects.equals(system.getFormStatus(), "CHANGED")) {

				// TODO CHECK_TO_COMMIT('DOWN') --- Program Unit Calling
				checkToCommit("DOWN");
				if (Objects.equals(system.getFormStatus(), "CHANGED")) {
					throw new FormTriggerFailureException();

				}

			}

			else {

				parameter.setUpdRec(coreptLib.setActionRestr(toChar(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "NXT"));

			}
			commonOncleardetail();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyNxtrec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyNxtrec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void preKeyexe(AirportStarTriggerRequestDto reqDto) throws Exception {

		try {
			system.setCoordinationOperation("EXECUTE_QUERY");
			mergeDelete();
			OracleHelpers.ResponseMapper(this, reqDto);
			commonOncleardetail();
			if (like("PRE%", parameter.getLibraryAccess())) {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					goBlock("PL_STD_STAR", "");
					system.setCursorBlock("PL_STD_STAR");
					plStdStar = new PlStdStar();
					plStdStarPreQuery(reqDto);
					plStdStarLeg.getData().add(0, new PlStdStarLeg());
					plStdStarLeg.getRow(0).setRecordStatus("NEW");
					plStdStarSegment.getData().add(0, new PlStdStarSegment());
					plStdStarSegment.getRow(0).setRecordStatus("NEW");

				}

				else {
					goBlock("PL_TLD_STAR", "");
					system.setCursorBlock("PL_TLD_STAR");
					plTldStar = new PlTldStar();
					plTldStarPreQuery(reqDto);
					plTldStarLeg.getData().add(0, new PlTldStarLeg());
					plTldStarLeg.getRow(0).setRecordStatus("NEW");

					plTldStarSegment.getData().add(0, new PlTldStarSegment());
					plTldStarSegment.getRow(0).setRecordStatus("NEW");

				}

			}

			else {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					goBlock("STD_STAR", "");
					system.setCursorBlock("STD_STAR");
					// stdStar = new tdStar();
					stdStar.add(0, new StdStar());

					stdStarPreQuery(reqDto);
					stdStarLeg.add(0, new StdStarLeg());
					stdStarSegment.add(0, new StdStarSegment());

				}

				else {
					goBlock("TLD_STAR", "");
					system.setCursorBlock("TLD_STAR");
					tldStar.add(0, new TldStar());
					tldStarPreQuery(reqDto);

					tldStarLeg.add(0, new TldStarLeg());
					tldStarSegment.add(0, new TldStarSegment());

				}

			}

			system.setCoordinationOperation("");
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}

	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyExeqry(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExeqry Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapperV2(reqDto, this, true);
			// onClearDetails(reqDto);

			if (Objects.equals(system.getMode(), "ENTER_QUERY")) {
				if (Objects.equals(system.getCursorBlock(), "PL_STD_STAR")) {
					system.setCursorBlock("PL_STD_STAR");
					plStdStarPreQuery(reqDto);
					plStdStarLeg.getData().add(0, new PlStdStarLeg());
					plStdStarLeg.getRow(0).setRecordStatus("NEW");

					plStdStarSegment.getData().add(0, new PlStdStarSegment());
					plStdStarSegment.getRow(0).setRecordStatus("NEW");

				} else if (Objects.equals(system.getCursorBlock(), "PL_TLD_STAR")) {
					plTldStarPreQuery(reqDto);
					plTldStarLeg.getData().add(0, new PlTldStarLeg());
					plTldStarLeg.getRow(0).setRecordStatus("NEW");
					plTldStarSegment.getData().add(0, new PlTldStarSegment());
					plTldStarSegment.getRow(0).setRecordStatus("NEW");

				} else if (Objects.equals(system.getCursorBlock(), "STD_STAR")) {
					stdStarPreQuery(reqDto);
					stdStarLeg.add(0, new StdStarLeg());
					stdStarSegment.add(0, new StdStarSegment());

				} else if (Objects.equals(system.getCursorBlock(), "TLD_STAR")) {
					tldStarPreQuery(reqDto);
					tldStarLeg.add(0, new TldStarLeg());

					tldStarSegment.add(0, new TldStarSegment());

				}

			}
			if (Objects.equals(system.getMode(), "NORMAL")) {
				if (!Objects.equals(system.getRecordStatus(), "NEW")
						&& !Objects.equals(parameter.getWorkType(), "VIEW")) {

					// TODO refresh_master_library.if_refresh;
					refreshMasterLibrary.ifRefresh();

				}

				system.setCoordinationOperation("EXECUTE_QUERY");
				mergeDelete();
				OracleHelpers.ResponseMapper(this, reqDto);
				commonOncleardetail();
				if (like("PRE%", parameter.getLibraryAccess())) {
					if (Objects.equals(parameter.getRecordType(), "S")) {
						goBlock("PL_STD_STAR", "");
						system.setCursorBlock("PL_STD_STAR");
						plStdStar = new PlStdStar();
						plStdStarPreQuery(reqDto);
						plStdStarLeg.getData().add(0, new PlStdStarLeg());
						plStdStarLeg.getRow(0).setRecordStatus("NEW");
						plStdStarSegment.getData().add(0, new PlStdStarSegment());
						plStdStarSegment.getRow(0).setRecordStatus("NEW");

					}

					else {
						goBlock("PL_TLD_STAR", "");
						system.setCursorBlock("PL_TLD_STAR");
						plTldStar = new PlTldStar();
						plTldStarPreQuery(reqDto);
						plTldStarLeg.getData().add(0, new PlTldStarLeg());
						plTldStarLeg.getRow(0).setRecordStatus("NEW");

						plTldStarSegment.getData().add(0, new PlTldStarSegment());
						plTldStarSegment.getRow(0).setRecordStatus("NEW");

					}

				}

				else {
					if (Objects.equals(parameter.getRecordType(), "S")) {
						goBlock("STD_STAR", "");
						system.setCursorBlock("STD_STAR");
						// stdStar = new tdStar();
						stdStar.add(0, new StdStar());

						stdStarPreQuery(reqDto);
						stdStarLeg.add(0, new StdStarLeg());
						stdStarSegment.add(0, new StdStarSegment());

					}

					else {
						goBlock("TLD_STAR", "");
						system.setCursorBlock("TLD_STAR");
						tldStar.add(0, new TldStar());
						tldStarPreQuery(reqDto);

						tldStarLeg.add(0, new TldStarLeg());
						tldStarSegment.add(0, new TldStarSegment());

					}

				}

			}
			if (nameIn(this, system.getCursorBlock() + ".default_where") != null) {
				copy(this, null, system.getCursorBlock() + ".default_where");
			}

			if (!OracleHelpers.isNullorEmpty(global.getGWhere())
					|| !OracleHelpers.isNullorEmpty(global.getGWhereSeg())) {
				setBlockProperty(system.getCursorBlock(), "default_where", global.getGWhere());
				system.setLastWhere(global.getGWhere());
				global.setGWhere(null);
				global.setGWhereSeg(null);
				copy(this, null, system.getCursorBlock() + ".lastQuery");
			}
			String value = "";
			if (like("PRE%", parameter.getLibraryAccess())) {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					value = plStdStar.getQueryWhere();
				} else {
					value = plTldStar.getQueryWhere();
				}
			}

			coreptLib.coreptexecutequery(this);
			if (like("PRE%", parameter.getLibraryAccess())) {
				if (Objects.equals(parameter.getRecordType(), "S")) {
					plStdStar.setQueryWhere(value);
				} else {
					plTldStar.setQueryWhere(value);
				}
			}
			callPostquery(system.getCursorBlock());
			setrelation();

			if (like("PRE%", parameter.getLibraryAccess())) {
				controlBlock.setCountInvalid(coreptLib.countInvalidRecords(system.getCursorBlock()));

			}
			if (like("PRE%", parameter.getLibraryAccess())) {
				if (!system.getFormStatus().equals("NEW")) {
					global.setCreateDcrNumber(toString(
							nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock()) + ".createDcrNumber")));
				}
			}

			if (like("PL_STD%", system.getCursorBlock())) {
				for (PlStdStarSegment pl : plStdStarSegment.getData()) {
					if (Objects.equals(pl.getAircraftType(), null)) {
						pl.setAircraftType("-");
					}
				}
				for (PlStdStarLeg pl : plStdStarLeg.getData()) {
					if (Objects.equals(pl.getAircraftType(), null)) {
						pl.setAircraftType("-");
					}
				}
			} else if (like("PL_TLD%", system.getCursorBlock())) {
				for (PlTldStarSegment pl : plTldStarSegment.getData()) {
					if (Objects.equals(pl.getAircraftType(), null)) {
						pl.setAircraftType("-");
					}
				}
				for (PlTldStarLeg pl : plTldStarLeg.getData()) {
					if (Objects.equals(pl.getAircraftType(), null)) {
						pl.setAircraftType("-");
					}
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyExeqry executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));

		} catch (FormTriggerFailureException ex) {
			plTldStar.setProcessingCycle(null);
			plStdStar.setProcessingCycle(null);
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(ex, resDto);
		} catch (Exception e) {
			log.error("Error while Executing the keyExeqry Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyEntqry(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyEntqry Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getMode(), "NORMAL")) {
				if (!Objects.equals(system.getRecordStatus(), "NEW")
						&& !Objects.equals(parameter.getWorkType(), "VIEW")) {

					// TODO refresh_master_library.if_refresh;
					refreshMasterLibrary.ifRefresh();

				}

				if (like("PRE%", parameter.getLibraryAccess())) {
					if (Objects.equals(parameter.getRecordType(), "S")) {
						goBlock("PL_STD_STAR", "");

					}

					else {
						goBlock("PL_TLD_STAR", "");

					}

				}

				else {
					if (Objects.equals(parameter.getRecordType(), "S")) {
						goBlock("STD_STAR", "");

					}

					else {
						goBlock("TLD_STAR", "");

					}

				}

			}

			globals.setGWhere(null);
			setBlockProperty(system.getCurrentBlock(), "default_where", "");

			// TODO corept_enter_query;
			coreptLib.coreptenterquery();

			system.setMode("ENTER_QUERY");
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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyCommit(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCommit Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			validateBeforeCommit(reqDto);
			preCommit(reqDto);

			if (Objects.equals(global.getClearBlock(), true)) {
				mergeDelete();
				commitForm(this);
				message("Record has been saved successfully");

				global.setClearBlock(false);
			} else {
				if (Objects.equals(parameter.getRecordType(), "S")
						|| Objects.equals(parameter.getEffectiveDel(), "Y")) {
					parameter.setEffectiveDel("N");

					// TODO CHECK_TO_COMMIT('COMMIT') --- Program Unit Calling
					checkToCommit("COMMIT");

				}

				else {

					// TODO Copy_Data_Check_PRC(plTldStar.getProcessingCycle());
					coreptLib.CopyDataCheckPrc(toInteger(plTldStar.getProcessingCycle()));

					if (Objects.equals(coreptLib.dcrEffectiveCycleFun(toInteger(plTldStar.getProcessingCycle()), null),
							"Y")) {

						// TODO CHECK_TO_COMMIT('COMMIT') --- Program Unit Calling
						checkToCommit("COMMIT");

					}

					else {
						throw new FormTriggerFailureException();

					}

				}
			}
			mergeDelete();
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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyDuprec(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDuprec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			}

			else {
				parameter.setUpdRec(coreptLib.setActionRestr(system.getCursorBlock(), global.getDataSupplier(),
						toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
						parameter.getRecordType(), "DUP"));
				// TODO parameter.setUpdRec(setActionRestr(nameIn(this,
				// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"DUP"));
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					if (like("%STAR", system.getCursorBlock())) {
						message("To copy a STAR record, please use the COPY_RECORDS function under the TOOL menu");

					}

					else {
						// if (like("%LEG", system.getCursorBlock())) {
						// if (!Objects.equals(nameIn(this, system.getCursorBlock() +
						// ".path_and_termination"), null)
						// || !Objects.equals(nameIn(this, system.getCursorBlock() +
						// ".waypoint_desc_code"),
						// null)) {
						// createRecord("");
						//
						// }
						//
						// }
						//
						// else if (!Objects.equals(nameIn(this, system.getCursorBlock() +
						// ".data_supplier"), null)) {
						// createRecord("");
						//
						// }
						//
						// // duplicateRecord( "");
						// if (like("%LEG", system.getCursorBlock())) {
						// copy(null, system.getCursorBlock() + ".validate_ind");
						//
						// }
						//
						// copy(null, system.getCursorBlock() + ".create_dcr_number");
						// copy(null, system.getCursorBlock() + ".update_dcr_number");
						// controlBlock.setValidated("N");

					}

				}

				else {
					if (like("%APPROACH", system.getCursorBlock())) {

						coreptLib.dspMsg("To Copy Approach Record use COPY_RECORDS fucntion under Tools Menu.");
						// TODO dsp_msg("To Copy Approach Record use COPY_RECORDS fucntion under Tools
						// Menu.");

					}

					else {
						coreptLib.dspMsg("Record cannot be Duplicated with DCR#" + global.getDcrNumber());
						// TODO dsp_msg("Record cannot be Duplicated with DCR#
						// "||global.getDcrNumber());
						throw new FormTriggerFailureException();

					}

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

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// keyDelrec(AirportStarTriggerRequestDto reqDto) throws Exception{
	// log.info(" keyDelrec Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// if(Objects.equals(parameter.getWorkType(), "VIEW")) {
	// null;
	//
	// }
	//
	//
	// else {
	// if(!Arrays.asList("NEW","INSERT").contains(system.getRecordStatus())) {
	// //TODO parameter.setUpdRec(setActionRestr(nameIn(this,
	// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"DEL"));
	// String pTableType = "M2C";
	// String vBlock = system.getCursorBlock();
	// Integer vDcrNumber = nameIn(this, substr(vBlock,1,11) +
	// ".create_dcr_number");
	// Integer vProcessingCycle = nameIn(this, vBlock + ".processing_cycle");
	// String vValidateInd = nameIn(this, substr(vBlock,1,11) + ".validate_ind");
	// String vStatus = system.getFormStatus();
	// String lsReturn = null;
	// String plStdStarSegmentCur = """
	// SELECT 1 FROM PL_STD_STAR_SEGMENT P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
	// """;
	// String plStdStarLegCur = """
	// SELECT 1 FROM PL_STD_STAR_LEG P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
	// """;
	// String plTldStarSegmentCur = """
	// SELECT 1 FROM PL_TLD_STAR_SEGMENT P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.customer_ident = ?
	// """;
	// String plTldStarLegCur = """
	// SELECT 1 FROM PL_TLD_STAR_LEG P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.customer_ident = ?
	// """;
	// String segmentPlStdLegCur = """
	// SELECT 1 FROM PL_STD_STAR_LEG P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.ROUTE_TYPE = ? and
	// p.TRANSITION_IDENT = ?
	// """;
	// String segmentPlTldLegCur = """
	// SELECT 1 FROM PL_TLD_STAR_LEG P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? AND P.CUSTOMER_IDENT = ? and
	// P.ROUTE_TYPE = ? and p.TRANSITION_IDENT = ?
	// """;
	// String dummyDefine = null;
	// String vExistMessage = null;
	// Integer vButton = 0;
	//
	// if(Objects.equals(vStatus, "CHANGED")) {
	//// TODO v_validate_ind :=
	// refresh_ml_utilities.get_validate_ind(substr(v_block,1,11),v_dcr_number)
	//
	// }
	//
	//
	// if(Arrays.asList("Y","S","H","O").contains(vValidateInd)) {
	// if(Objects.equals(refreshMasterLibrary.check_reference_info(vBlock,"D"),
	// "N")) {
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	//
	// }
	//
	//
	// if(Objects.equals(vBlock, "PL_STD_STAR")) {
	// List<Record> records = app.executeQuery(plStdStarSegmentCur);
	//// TODO FETCHPL_STD_STAR_SEGMENT_curINTODummy_Define
	// if((plStdStarSegmentCur%found)) {
	// vExistMessage = "There are existing segments and/or legs.";
	//
	// }
	//
	//
	// else {
	// List<Record> records = app.executeQuery(plStdStarLegCur);
	//// TODO FETCHPL_STD_STAR_LEG_curINTODummy_Define
	// if((plStdStarLegCur%found)) {
	// vExistMessage = "There are existing legs.";
	//
	// }
	//
	//
	//// TODO CLOSEPL_STD_STAR_LEG_cur
	//
	// }
	//// TODO CLOSEPL_STD_STAR_SEGMENT_cur
	//
	// }
	//
	//
	// else if(Objects.equals(vBlock, "PL_TLD_STAR")) {
	// List<Record> records = app.executeQuery(plTldStarSegmentCur);
	//// TODO FETCHPL_TLD_STAR_SEGMENT_curINTODummy_Define
	// if((plTldStarSegmentCur%found)) {
	// vExistMessage = "There are existing segments and/or legs.";
	//
	// }
	//
	//
	// else {
	// List<Record> records = app.executeQuery(plTldStarLegCur);
	//// TODO FETCHPL_TLD_STAR_LEG_curINTODummy_Define
	// if((plTldStarLegCur%found)) {
	// vExistMessage = "There are existing legs.";
	//
	// }
	//
	//
	//// TODO CLOSEPL_TLD_STAR_LEG_cur
	//
	// }
	//// TODO CLOSEPL_TLD_STAR_SEGMENT_cur
	//
	// }
	//
	//
	// else if(Objects.equals(vBlock, "PL_STD_STAR_SEGMENT")) {
	// List<Record> records = app.executeQuery(segmentPlStdLegCur);
	//// TODO FETCHSEGMENT_PL_STD_LEG_curINTODummy_Define
	// if((segmentPlStdLegCur%found)) {
	// vExistMessage = "There are existing legs.";
	//
	// }
	//
	//
	//// TODO CLOSESEGMENT_PL_STD_LEG_cur
	//
	// }
	//
	//
	// else if(Objects.equals(vBlock, "PL_TLD_STAR_SEGMENT")) {
	// List<Record> records = app.executeQuery(segmentPlTldLegCur);
	//// TODO FETCHSEGMENT_PL_TLD_LEG_curINTODummy_Define
	// if((segmentPlTldLegCur%found)) {
	// vExistMessage = "There are existing legs.";
	//
	// }
	//
	//
	//// TODO CLOSESEGMENT_PL_TLD_LEG_cur
	//
	// }
	//
	//
	// if(Objects.equals(parameter.getRecordType(), "T")) {
	// //TODO lsReturn = dcrEffectiveCycleFun(plTldStar.getProcessingCycle(),"D");
	// if(Objects.equals(lsReturn, "Y")) {
	// parameter.setEffectiveDel("Y");
	//
	// }
	//
	//
	//
	// }
	//
	//
	// if(Objects.equals(lsReturn, "N")) {
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	// if(!Objects.equals(vExistMessage, null)) {
	// if(like( "%STAR",vBlock)) {
	//// TODO v_button := DISPLAY_ALERT.MORE_BUTTONs('S','DELETE PROCEDURE',
	// v_exist_MESSAGE||chr(10)|| 'Pick your choice
	// carefully:'||chr(10)||chr(10),'Delete All','Cancel')
	// if(Objects.equals(vButton, 1)) {
	// if(Objects.equals(vBlock, "PL_STD_STAR")) {
	//
	// query = """
	// DELETE FROM PL_STD_STAR_SEGMENT P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
	// """ ;
	// app.ExecuteNonQuery(query,plStdStar.getAirportIdent(),plStdStar.getAirportIcao(),plStdStar.getStarIdent(),plStdStar.getDataSupplier(),plStdStar.getProcessingCycle());
	// query = """
	// DELETE FROM PL_STD_STAR_LEG P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
	// """ ;
	// app.ExecuteNonQuery(query,plStdStar.getAirportIdent(),plStdStar.getAirportIcao(),plStdStar.getStarIdent(),plStdStar.getDataSupplier(),plStdStar.getProcessingCycle());
	// }
	//
	//
	// else if(Objects.equals(vBlock, "PL_TLD_STAR")) {
	//
	// query = """
	// DELETE FROM PL_TLD_STAR_SEGMENT P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.customer_ident = ?
	// """ ;
	// app.ExecuteNonQuery(query,plTldStar.getAirportIdent(),plTldStar.getAirportIcao(),plTldStar.getStarIdent(),plTldStar.getDataSupplier(),plTldStar.getProcessingCycle(),plTldStar.getCustomerIdent());
	// query = """
	// DELETE FROM PL_TLD_STAR_LEG P
	// WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
	// P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.customer_ident = ?
	// """ ;
	// app.ExecuteNonQuery(query,plTldStar.getAirportIdent(),plTldStar.getAirportIcao(),plTldStar.getStarIdent(),plTldStar.getDataSupplier(),plTldStar.getProcessingCycle(),plTldStar.getCustomerIdent());
	// }
	//
	//
	//
	// }
	//
	//
	// else {
	// parameter.setEffectiveDel("N");
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else if(like( "%SEGMENT",vBlock)) {
	//// TODO v_button := DISPLAY_ALERT.MORE_BUTTONs('S','DELETE PROCEDURE',
	// v_exist_MESSAGE||chr(10)|| 'Pick your choice
	// carefully:'||chr(10)||chr(10),'Delete Legs too','Delete Segment
	// only','Cancel')
	// if(Objects.equals(vButton, 1)) {
	// if(Objects.equals(vBlock, "PL_STD_STAR_SEGMENT")) {
	// goBlock("PL_STD_STAR_LEG", "");
	//
	// //TODO first_record;
	// while(true)
	// {
	// if(Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(),
	// plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()) &&
	// Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
	// plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()))
	// {
	// if(Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRefInfo(),
	// null)) {
	// parameter.setUpdDcrDel("Y");
	// deleteRecord( "");
	//
	// }
	//
	//
	// else {
	// controlBlock.setStdValidationErrors("The leg with.se9# " +
	// plStdStarLeg.getRow(system.getCursorRecordIndex()).getSe9uenceNum() + " of
	// the seg: " +
	// plStdStarLeg.getRow(system.getCursorRecordIndex()).getRefInfo());
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else {
	// break;
	// nextRecord( "");
	//
	// }
	//
	// }
	//
	// //TODO first_record;
	// setItemProperty("control_block.std_leg_errors", FormConstant.VISIBLE,
	// FormConstant.PROPERTY_FALSE);
	// goBlock("PL_STD_STAR_SEGMENT", "");
	//
	// }
	//
	//
	// else if(Objects.equals(vBlock, "PL_TLD_STAR_SEGMENT")) {
	// goBlock("PL_TLD_STAR_LEG", "");
	//
	// //TODO first_record;
	// while(true)
	// {
	// if(Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType(),
	// plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType()) &&
	// Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
	// plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent()))
	// {
	// if(Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRefInfo(),
	// null)) {
	// parameter.setUpdDcrDel("Y");
	// deleteRecord( "");
	//
	// }
	//
	//
	// else {
	// controlBlock.setTldValidationErrors("The leg with.se9# " +
	// plTldStarLeg.getRow(system.getCursorRecordIndex()).getSe9uenceNum() + " of
	// the seg: " +
	// plTldStarLeg.getRow(system.getCursorRecordIndex()).getRefInfo());
	// throw new FormTriggerFailureException();
	//
	// }
	//
	// }
	//
	//
	// else {
	// break;
	// nextRecord( "");
	//
	// }
	//
	// }
	//
	// //TODO first_record;
	// setItemProperty("control_block.tld_leg_errors", FormConstant.VISIBLE,
	// FormConstant.PROPERTY_FALSE);
	// goBlock("PL_TLD_STAR_SEGMENT", "");
	//
	// }
	//
	//
	//
	// }
	//
	//
	// else if(Objects.equals(vButton, 3)) {
	// parameter.setEffectiveDel("N");
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	//
	// }
	//
	//
	//
	// }
	//
	//
	// else {
	// if(like( "%LEG",vBlock)) {
	// if(Objects.equals(displayAlert.MORE_BUTTONs("S","DELETE A LEG","Please be
	// sure that you want to delete this leg." + chr(10) +
	// chr(10),"Delete","Cancel"), 2)) {
	// parameter.setEffectiveDel("N");
	// throw new FormTriggerFailureException();
	//
	// }
	//
	//
	//
	// }
	//
	//
	//
	// }
	// if(Arrays.asList("Y","S","H","O").contains(vValidateInd) && like(
	// "%STAR",vBlock)) {
	//
	// //TODO refresh_master_library.delete_from_ref_table(v_dcr_number);
	//
	// }
	//
	//
	// if(Objects.equals(global.getLibRefreshed(), "Y") &&
	// Arrays.asList(global.getNewProcessingCycle(),global.getOldProcessingCycle()).contains(vProcessingCycle)
	// && Arrays.asList("Y","S","H","O").contains(vValidateInd)) {
	// if(like( "%STAR",vBlock)) {
	//
	// //TODO dsp_msg("System is going to refresh this deletion for the master
	// library table.");
	//
	// //TODO
	// refresh_master_library.refresh_a_record(p_table_type,v_dcr_number,v_processing_cycle,substr(v_block,4,3)||"_STAR","I");
	// deleteRecord( "");
	//
	// //TODO commit_form;
	//
	// //TODO
	// refresh_master_library.set_record_group(v_dcr_number,"I",substr(v_block,4,3)||"_STAR",v_processing_cycle,"D");
	// parameter.setEffectiveDel("N");
	//
	// //TODO dsp_msg("Refresh Successful and the deletion is commited.");
	//
	// }
	//
	//
	// else {
	//
	// //TODO
	// refresh_master_library.set_record_group(v_dcr_number,v_validate_ind,substr(v_block,1,11),v_processing_cycle,"D");
	// parameter.setUpdDcrDel("Y");
	// deleteRecord( "");
	//
	// }
	//
	// }
	//
	//
	// else {
	// parameter.setUpdDcrDel("Y");
	// deleteRecord( "");
	// if(like( "%STAR",vBlock)) {
	// parameter.setEffectiveDel("N");
	//
	// //TODO commit_form;
	//
	// }
	//
	//
	//
	// }
	//
	// }
	//
	//
	// else {
	// deleteRecord( "");
	//
	// }
	// controlBlock.setValidated("N");
	//
	// }
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" keyDelrec executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the keyDelrec Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> preCommit(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" preCommit Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(
					nameIn(this, "control_block." + substr(system.getCursorBlock(), 4, 3) + "_VALIDATION_ERRORS"), null)
					&& !Objects.equals(nameIn(this, substr(system.getCursorBlock(), 1, 7) + "star.ref_info"), null)) {
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" preCommit executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the preCommit Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyExit(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExit Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// Object lsFormId = null;
			// String lsDataType = null;
			// Object lsProcessId = null;
			//
			// //TODO lsFormId = findForm(lower("AIRPORT_STAR"));
			// lsDataType = TO_CHAR(ls_Form_Id.ID);
			// if(plotGlobalPkg.ltab_Process_ID.COUNT// TODO
			// Plot_Global_Pkg.ltab_Process_ID.COUNT >= 1) {
			// for(int plot_exe_no = Plot_Global_Pkg.ltab_Process_ID.FIRST;plot_exe_no <=
			// Plot_Global_Pkg.ltab_Process_ID.LAST;plot_exe_no++)
			// {
			// if(Objects.equals(plotGlobalPkg.ltab_Process_ID(plotExeNo), lsDataType)) {
			// //TODO lsProcessId.handle =
			// toNumber(plotGlobalPkg.ltab_Process_ID(plotExeNo));
			//
			// //TODO WEBUTIL_HOST.Terminate_Process(ls_process_id);
			//
			// }
			//
			//
			//
			// }
			//
			// }

			if (Objects.equals(system.getMode(), "NORMAL")) {

				// TODO CHECK_TO_COMMIT('EXIT') --- Program Unit Calling
				checkToCommit("EXIT");

			} else {
				exitForm();
			}

			// TODO Set_Application_Property(cursor_style,"DEFAULT");
			setApplicationProperty(CURSOR_STYLE, "DEFAULT");

			// TODO Exit_form;

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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> whenFormNavigate(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" whenFormNavigate Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getFormStatus(), "CHANGED")
					&& Objects.equals(nameIn(this, "global.check_save"), "Y")) {

				// TODO Check_Save_PRC("Airport STAR");
				coreptLib.checkSavePrc("Airport STAR");

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

	void plStdStarWhenNewRecordInstance() throws Exception {
		if (Objects.equals(system.getMode(), "NORMAL")) {
			if (!Objects.equals(plStdStar.getAirportIdent(), null)
					&& (Objects.equals(system.getFormStatus(), "CHANGED")
							|| !Objects.equals(nvl(plStdStar.getValidateInd(), "N"), "Y"))
					&& (Objects.equals(controlBlock.getValidated(), "N")
							|| !Objects.equals(controlBlock.getTempDcr(), toInteger(plStdStar.getCreateDcrNumber())))) {

				// TODO validate_procedure --- Program Unit Calling
				validateProcedure("Y");

			}

			parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
					global.getDataSupplier(), toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
					parameter.getRecordType(), "UPD"));

			// TODO if_from_leg_search(2);
			coreptLib.iffromlegsearch(2);
			controlBlock.setTempDcr(toInteger(nvl(toString(plStdStar.getCreateDcrNumber()), "0")));
			if (!Objects.equals(controlBlock.getStdOverrideErrors(), null)) {
				if (Objects.equals(getItemProperty("CONTROL_BLOCK.STD_OVERIDE", FormConstant.ENABLED), "FALSE")) {
					setBlockItemProperty("control_block.std_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_TRUE);
					setBlockItemProperty("control_block.std_overide", FormConstant.ENABLED, FormConstant.PROPERTY_TRUE);

				}

			}

			else {
				setBlockItemProperty("control_block.std_overide", FormConstant.ENABLED, FormConstant.PROPERTY_FALSE);
				setBlockItemProperty("control_block.std_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

			}

		}

		mergeDelete();
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarWhenNewRecordInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarWhenNewRecordInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getMode(), "NORMAL")) {
				if (!Objects.equals(plStdStar.getAirportIdent(), null)
						&& (Objects.equals(system.getFormStatus(), "CHANGED")
								|| !Objects.equals(nvl(plStdStar.getValidateInd(), "N"), "Y"))
						&& (Objects.equals(controlBlock.getValidated(), "N") || !Objects
								.equals(controlBlock.getTempDcr(), toInteger(plStdStar.getCreateDcrNumber())))) {

					// TODO validate_procedure --- Program Unit Calling
					validateProcedure("Y");

				}

				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

				// TODO if_from_leg_search(2);
				coreptLib.iffromlegsearch(2);
				controlBlock.setTempDcr(toInteger(nvl(toString(plStdStar.getCreateDcrNumber()), "0")));
				if (!Objects.equals(controlBlock.getStdOverrideErrors(), null)) {
					if (Objects.equals(getItemProperty("CONTROL_BLOCK.STD_OVERIDE", FormConstant.ENABLED), "FALSE")) {
						setBlockItemProperty("control_block.std_overide", FormConstant.VISIBLE,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("control_block.std_overide", FormConstant.ENABLED,
								FormConstant.PROPERTY_TRUE);

					}

				}

				else {
					setBlockItemProperty("control_block.std_overide", FormConstant.ENABLED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("control_block.std_overide", FormConstant.VISIBLE,
							FormConstant.PROPERTY_FALSE);

				}

			}

			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void plStdStarPreQuery(AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarPreQuery Executing");

		try {
			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				plStdStar.setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plStdStar.setProcessingCycle(global.getProcessingCycle());

				}

			}

			log.info(" plStdStarPreQuery executed successfully");
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void plStdStarPostQuery() throws Exception {
		log.info(" plStdStarPostQuery Executing");

		try {
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				plStdStar.setOldValidateInd(plStdStar.getValidateInd());
				plStdStar.setRefInfo(coreptLib.getRefInfo("PL_STD_STAR", toInteger(plStdStar.getCreateDcrNumber())));
				controlBlock.setValidated("N");
				controlBlock.setStdValidationErrors(null);

			}

			log.info(" plStdStarPostQuery executed successfully");

		} catch (Exception e) {
			log.error("Error while Executing the plStdStarPostQuery Service");
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarWhenValidateRecord(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarWhenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {

			OracleHelpers.bulkClassMapper(reqDto, this);
			// plStdStarAirportIdentWhenValidateItem(reqDto);
			plStdStarAirportIcaoWhenValidateItem(reqDto);
			plStdStarStarIdentWhenValidateItem(reqDto);
			plStdStarSpecialsIndWhenValidateItem(reqDto);

			String query = null;
			Record rec = null;
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects.equals(system.getRecordStatus(), "QUERY")
					&& Objects.equals(system.getCursorBlock(), "PL_STD_STAR")) {
				if (!Arrays.asList("Y", "S", "H", "W", "N", "I", "O").contains(nvl(plStdStar.getValidateInd(), "Y"))
						|| (Arrays.asList("Y", "S", "H", "W", "N", "O")
								.contains(nvl(plStdStar.getOldValidateInd(), "Y"))
								&& !Arrays.asList("Y", "S", "H", "W", "N", "O")
										.contains(nvl(plStdStar.getValidateInd(), "Y")))) {

					coreptLib.dspMsg("You can only update the validate_ind to 'Y','S','H','W' or 'N'");
					plStdStar.setValidateInd(plStdStar.getOldValidateInd());

				}

				if (Objects.equals(plStdStar.getValidateInd(), null)
						&& !Objects.equals(plStdStar.getOldValidateInd(), null)) {
					plStdStar.setValidateInd(plStdStar.getOldValidateInd());

				}

				if (!Objects.equals(plStdStar.getRefInfo(), null)
						&& Arrays.asList("W", "N", "I").contains(plStdStar.getValidateInd())) {
					controlBlock.setStdValidationErrors(plStdStar.getRefInfo());
					setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
							FormConstant.PROPERTY_TRUE);
					setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
							FormConstant.PROPERTY_TRUE);
					plStdStar.setValidateInd(plStdStar.getOldValidateInd());

				}

				if (Objects.equals(plStdStar.getProcessingCycle(), null)) {
					plStdStar.setProcessingCycle(global.getProcessingCycle());

				}

				if (Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					if (Objects.equals(checkProcPk(), false)) {

						coreptLib.dspMsg("This record already exists!");
						throw new FormTriggerFailureException(event);

					}

					plStdStar.setDataSupplier(global.getDataSupplier());
					if (Objects.equals(plStdStar.getCreateDcrNumber(), null)) {

						query = """
								select dcr_number_seq.nextval from dual
								""";
						rec = app.selectInto(query);
						plStdStar.setCreateDcrNumber(new CustomInteger(toString(rec.getInt())));
						controlBlock.setTempDcr(toInteger(plStdStar.getCreateDcrNumber()));

					}

					plStdStar.setValidateInd("I");
					plStdStar.setOldValidateInd("I");
					controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);

				}

				controlBlock.setValidated("N");

				// TODO validate_keys --- Program Unit Calling
				validateKeys("Y");
				if (Objects.equals(global.getLibRefreshed(), "Y") && (Objects.equals(plStdStar.getOldValidateInd(), "Y")
						|| Objects.equals(plStdStar.getValidateInd(), "Y"))) {
					if (Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle())
							.contains(toChar(plStdStar.getProcessingCycle()))) {

						refreshMasterLibrary.setRecordGroup(toInteger(plStdStar.getCreateDcrNumber()),
								plStdStar.getValidateInd(), "PL_STD_STAR", toInteger(plStdStar.getProcessingCycle()),
								"U");

					}

				}
				
				for (PlStdStarSegment plStdStarSegment : plStdStarSegment.getData()) {
					if(!Objects.equals(plStdStarSegment.getProcessingCycle(), plStdStar.getProcessingCycle())) {
						plStdStarSegment.setProcessingCycle(plStdStar.getProcessingCycle());
					}
				}
				
				for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
					if(!Objects.equals(plStdStarLeg.getProcessingCycle(), plStdStar.getProcessingCycle())) {
						plStdStarLeg.setProcessingCycle(plStdStar.getProcessingCycle());
					}
				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarPostDelete(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarPostDelete Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plStdStar.getValidateInd(), "I")) {
				controlBlock.setCountInvalid(controlBlock.getCountInvalid() - 1);

			}

			setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarPostDelete executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarPostDelete Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarOnPopulateDetails(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarOnPopulateDetails Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String recstat = system.getRecordStatus();
			String startitm = system.getCursorItem();
			Object relId = null;

			if ((Objects.equals(recstat, "NEW") || Objects.equals(recstat, "INSERT"))) {

				return null;
			}

			if (((!Objects.equals(plStdStar.getAirportIdent(), null))
					|| (!Objects.equals(plStdStar.getAirportIcao(), null))
					|| (!Objects.equals(plStdStar.getStarIdent(), null))
					|| (!Objects.equals(plStdStar.getDataSupplier(), null))
					|| (!Objects.equals(plStdStar.getProcessingCycle(), null)))) {
				// TODO relId = findRelation("PL_STD_STAR.PL_STD_STAR_PL_STD_STAR_SE");

				// TODO Query_Master_Details(rel_id,'PL_STD_STAR_SEGMENT') --- Program Unit
				// Calling
				queryMasterDetails(relId, "PL_STD_STAR_SEGMENT");

			}

			if (((!Objects.equals(plStdStar.getAirportIdent(), null))
					|| (!Objects.equals(plStdStar.getAirportIcao(), null))
					|| (!Objects.equals(plStdStar.getStarIdent(), null))
					|| (!Objects.equals(plStdStar.getDataSupplier(), null))
					|| (!Objects.equals(plStdStar.getProcessingCycle(), null)))) {
				// TODO relId = findRelation("PL_STD_STAR.PL_STD_STAR_PL_STD_STAR_LEG");

				// TODO Query_Master_Details(rel_id,'PL_STD_STAR_LEG') --- Program Unit Calling
				queryMasterDetails(relId, "PL_STD_STAR_LEG");

			}

			if ((!Objects.equals(system.getCursorItem(), startitm))) {
				// goItem(startitm);

				// TODO Check_Package_Failure --- Program Unit Calling
				checkPackageFailure();

			}

			if (Objects.equals(plStdStar.getValidateInd(), "I")) {
				setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_TRUE);
				setItemProperty("control_block.std_validation_errors", FormConstant.ENABLED,
						FormConstant.PROPERTY_TRUE);

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarOnPopulateDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarOnPopulateDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarAirportIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarAirportIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (instr(plStdStar.getAirportIdent(), "%") > 0 || instr(plStdStar.getAirportIdent(), "_") > 0) {

				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");
				plStdStar.setAirportIdent(null);
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarAirportIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarAirportIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarAirportIcaoWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarAirportIcaoWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (instr(plStdStar.getAirportIcao(), "%") > 0 || instr(plStdStar.getAirportIcao(), "_") > 0) {
				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");

				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				plStdStar.setAirportIcao(null);
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarAirportIcaoWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarAirportIcaoWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarStarIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarStarIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (instr(plStdStar.getStarIdent(), "%") > 0 || instr(plStdStar.getStarIdent(), "_") > 0) {

				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");

				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				plStdStar.setStarIdent(null);
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarStarIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarStarIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSpecialsIndWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSpecialsIndWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(rtrim(plStdStar.getSpecialsInd()), null)) {
					plStdStar.setSpecialsInd("N");

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSpecialsIndWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSpecialsIndWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarValidateIndPreTextItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarValidateIndPreTextItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && Objects.equals(plStdStar.getValidateInd(), "I")) {
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarValidateIndPreTextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarValidateIndPreTextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentWhenNewRecordInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentWhenNewRecordInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Arrays.asList("INSERT", "NEW").contains(system.getRecordStatus())) {
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "CRE"));

			}

			else {
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

			}
			if (!Objects.equals(parameter.getWorkType(), "VIEW")
					&& !Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(), null)) {
				if (Objects.equals(refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "W"), "N")) {
					setBlockItemProperty("pl_std_star_segment.route_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("pl_std_star_segment.transition_ident", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("pl_std_star_segment.aircraft_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);

				}

				else {
					setBlockItemProperty("pl_std_star_segment.route_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);
					setBlockItemProperty("pl_std_star_segment.transition_ident", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);
					setBlockItemProperty("pl_std_star_segment.aircraft_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);

				}

			}

			if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {

				// TODO first_record;
				for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
					if (Objects.equals(plStdStarLeg.getRouteType(), controlBlock.getOldRouteType())
							&& Objects.equals(plStdStarLeg.getTransitionIdent(), controlBlock.getOldTransitionIdent())
							&& Objects.equals(plStdStarLeg.getAircraftType(), controlBlock.getOldAircraftType())) {
						plStdStarLeg.setRouteType(controlBlock.getNewRouteType());
						plStdStarLeg.setTransitionIdent(controlBlock.getNewTransitionIdent());
						plStdStarLeg.setAircraftType(controlBlock.getNewAircraftType());

					}

				}

				// TODO first_record;
				controlBlock.setPkSegmentChange("N");
				controlBlock.setPkAircraftChange("N");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("N");

			}

			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentWhenValidateRecord(
			AirportStarTriggerRequestDto reqDto) throws Exception {

		log.info(" plStdStarSegmentWhenValidateRecord Executing");
		Record rec = null;
		String query = null;
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			plStdStarSegmentRouteTypeWhenValidateItem(reqDto);
			plStdStarSegmentTransitionIdentWhenValidateItem(reqDto);
			plStdStarSegmentQualifier1WhenValidateItem(reqDto);
			plStdStarSegmentQualifier2WhenValidateItem(reqDto);
			plStdStarSegmentAircraftTypeWhenValidateItem(reqDto);
			plStdStarSegmentProcDesignMagVarIndWhenValidateItem(reqDto);
			plStdStarSegmentCreateDcrNumberWhenValidateItem(reqDto);

			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects.equals(system.getBlockStatus(), "QUERY")) {
				if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {
					if (Objects.equals(checkSegmentPk(), false)) {

						// TODO dsp_msg("This change will generate a duplicate record! ");
						coreptLib.dspMsg("This change will generate a duplicate record! ");
						plStdStarSegment.getRow(system.getCursorRecordIndex())
								.setRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
						plStdStarSegment.getRow(system.getCursorRecordIndex()).setTransitionIdent(
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
						plStdStarSegment.getRow(system.getCursorRecordIndex()).setAircraftType(
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
						controlBlock.setPkSegmentChange("N");
						controlBlock.setPkAircraftChange("N");
						controlBlock.setPkRouteChange("N");
						controlBlock.setPkTransitionChange("N");

					}

				}

				if (Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getCreateDcrNumber(), null)) {

					query = """
									select dcr_number_seq.nextval from dual
							""";
					rec = app.selectInto(query);
					plStdStarSegment.getRow(system.getCursorRecordIndex())
							.setCreateDcrNumber(new CustomInteger(toString(rec.getInt())));
					plStdStarSegment.getRow(system.getCursorRecordIndex())
							.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
					plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
							plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
					plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldAircraftType(
							plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
					controlBlock.setPkSegmentChange("N");
					controlBlock.setPkAircraftChange("N");
					controlBlock.setPkRouteChange("N");
					controlBlock.setPkTransitionChange("N");

				}

				controlBlock.setValidated("N");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void plStdStarSegmentPostQuery() throws Exception {
		log.info(" plStdStarSegmentPostQuery Executing");
		// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
		// BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				for (PlStdStarSegment plStdStarSegment : plStdStarSegment.getData()) {
					plStdStarSegment.setOldRouteType(plStdStarSegment.getRouteType());
					plStdStarSegment.setOldTransitionIdent(plStdStarSegment.getTransitionIdent());
					plStdStarSegment.setOldAircraftType(plStdStarSegment.getAircraftType());
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentPostQuery executed successfully");

		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentKeyCrerec(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentKeyCrerec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// String lcCurRec = null;

			parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
					global.getDataSupplier(), toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
					parameter.getRecordType(), "CRE"));
			if (Objects.equals(parameter.getUpdRec(), "Y")) {
				if (Objects.equals(controlBlock.getPkSegmentChange(), "N")
						&& Objects.equals(system.getCursorBlock(), "PL_STD_STAR_SEGMENT")
						&& (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())
								|| !Objects.equals(
										plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
										plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())
								|| !Objects.equals(
										plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
										plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType()))) {
					// lcCurRec = system.getCursorRecord();
					// goBlock("PL_STD_STAR_LEG", "");

					// TODO first_record;
					for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
						if (Objects.equals(plStdStarLeg.getRouteType(), controlBlock.getOldRouteType())
								&& Objects.equals(plStdStarLeg.getTransitionIdent(),
										controlBlock.getOldTransitionIdent())
								&& Objects.equals(plStdStarLeg.getAircraftType(), controlBlock.getOldAircraftType())) {
							plStdStarLeg.setRouteType(controlBlock.getNewRouteType());
							plStdStarLeg.setTransitionIdent(controlBlock.getNewTransitionIdent());
							plStdStarLeg.setAircraftType(controlBlock.getNewAircraftType());

						}

						// nextRecord( "");

					}

					// TODO first_record;
					controlBlock.setPkSegmentChange("N");
					controlBlock.setPkRouteChange("N");
					controlBlock.setPkTransitionChange("N");
					controlBlock.setPkAircraftChange("N");
					// goBlock("PL_STD_STAR_SEGMENT", "");

					// TODO go_record(lc_cur_rec);

				}

				createRecord("");
				PlStdStarSegment seg = new PlStdStarSegment();
				seg.setAircraftType("-");
				plStdStarSegment.getData().add(system.getCursorRecordIndex() + 1, seg);
				plStdStarSegment.getRow(system.getCursorRecordIndex() + 1).setRecordStatus("NEW");
				system.setFormStatus("CHANGED");
				// goItem("pl_std_star_segment.route_type");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentKeyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentKeyCrerec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentRouteTypeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentRouteTypeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo(system.getCursorBlock(),
				// "C");
				refreshMasterLibrary.checkReferenceInfo(system.getCursorBlock(), "C");
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(toString(nameIn(this, system.getCursorBlock() + ".processing_cycle")),
									global.getProcessingCycle())),
							Objects.equals(parameter.getRecordType(), "S") ? null
									: toString(nameIn(this, system.getCursorBlock() + ".customer_ident")));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentRouteTypeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentRouteTypeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentRouteTypeWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentRouteTypeWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())
					&& (Objects.equals(controlBlock.getPkAircraftChange(), "Y")
							|| Objects.equals(controlBlock.getPkTransitionChange(), "Y"))) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setPkRouteChange("Y");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkSegmentChange("Y");

			}

			else if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkRouteChange("Y");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("N");
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentRouteTypeWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentRouteTypeWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentTransitionIdentOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentTransitionIdentOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage =
				// this.refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "C");
				this.refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "C");

				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {
					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(toString(nameIn(this, system.getCursorBlock() + ".processing_cycle")),
									global.getProcessingCycle())),
							null);

					// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),nvl(nameIn(this,system.getCursorBlock()+".processing_cycle"),global.getProcessingCycle()));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentTransitionIdentOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentTransitionIdentOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentTransitionIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentTransitionIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())
					&& (Objects.equals(controlBlock.getPkRouteChange(), "Y")
							|| Objects.equals(controlBlock.getPkAircraftChange(), "Y"))) {
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setPkSegmentChange("Y");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("Y");
				controlBlock.setPkAircraftChange("N");

			}

			else if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("Y");
				controlBlock.setPkAircraftChange("N");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentTransitionIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentTransitionIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentTransitionIdentWhenMouseDoubleclick(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentTransitionIdentWhenMouseDoubleclick Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("pl_std_star_leg.transition_ident");

			// TODO first_record;
			while (true) {
				if (Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {

					// TODO next_item;
					break;

				}

				else {
					nextRecord("");

				}
				break;

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentTransitionIdentWhenMouseDoubleclick executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentTransitionIdentWhenMouseDoubleclick Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentQualifier1OnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentQualifier1OnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "C");
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							null);

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentQualifier1OnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentQualifier1OnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentQualifier1WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentQualifier1WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentQualifier1WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentQualifier1WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentQualifier2OnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentQualifier2OnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "C");

				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							null);

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentQualifier2OnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentQualifier2OnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentQualifier2WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentQualifier2WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentQualifier2WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentQualifier2WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentAircraftTypeWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentAircraftTypeWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())
					&& (Objects.equals(controlBlock.getPkRouteChange(), "Y")
							|| Objects.equals(controlBlock.getPkTransitionChange(), "Y"))) {
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("Y");

			}

			else if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentAircraftTypeWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentAircraftTypeWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentAircraftTypeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentAircraftTypeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "C");

				// vMessage = refreshMasterLibrary.checkRefrenceInfo('PL_STD_STAR_SEGMENT','C');
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							null);

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentAircraftTypeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentAircraftTypeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentProcDesignMagVarIndWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentProcDesignMagVarIndWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())
					&& (Objects.equals(controlBlock.getPkRouteChange(), "Y")
							|| Objects.equals(controlBlock.getPkTransitionChange(), "Y"))) {
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("Y");

			}

			else if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentProcDesignMagVarIndWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentProcDesignMagVarIndWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentProcDesignMagVarIndOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentProcDesignMagVarIndOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_STD_STAR_SEGMENT','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentProcDesignMagVarIndOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentProcDesignMagVarIndOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentCreateDcrNumberOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentCreateDcrNumberOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_SEGMENT", "C");

				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_STD_STAR_SEGMENT','C')
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							null);

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentCreateDcrNumberOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentCreateDcrNumberOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarSegmentCreateDcrNumberWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarSegmentCreateDcrNumberWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
					plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {
				controlBlock.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plStdStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plStdStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarSegmentCreateDcrNumberWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarSegmentCreateDcrNumberWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void plStdStarLegPostQuery() throws Exception {
		log.info(" plStdStarLegPostQuery Executing");
		// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
		// BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
				if (!Objects.equals(plStdStarLeg.getWaypointDescCode(), null)) {
					plStdStarLeg.setWc1(substr(rpad(plStdStarLeg.getWaypointDescCode(), 4), 1, 1).trim());
					plStdStarLeg.setWc2(substr(rpad(plStdStarLeg.getWaypointDescCode(), 4), 2, 1).trim());
					plStdStarLeg.setWc3(substr(rpad(plStdStarLeg.getWaypointDescCode(), 4), 3, 1).trim());
					plStdStarLeg.setWc4(substr(rpad(plStdStarLeg.getWaypointDescCode(), 4), 4, 1).trim());
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegPostQuery executed successfully");
			// return
			// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
			// resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			// return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWhenNewBlockInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWhenNewBlockInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {

					// TODO first_record;
					for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
						if (Objects.equals(plStdStarLeg.getRouteType(), controlBlock.getOldRouteType())
								&& Objects.equals(plStdStarLeg.getTransitionIdent(),
										controlBlock.getOldTransitionIdent())
								&& Objects.equals(plStdStarLeg.getAircraftType(), controlBlock.getOldAircraftType())) {
							plStdStarLeg.setRouteType(controlBlock.getNewRouteType());
							plStdStarLeg.setTransitionIdent(controlBlock.getNewTransitionIdent());
							plStdStarLeg.setAircraftType(controlBlock.getNewAircraftType());
							if (Objects.equals("QUERIED", plStdStarLeg.getRecordStatus())) {

								plStdStarLeg.setRecordStatus("CHANGED");
							}

						}

						// nextRecord( "");

					}
					controlBlock.setPkSegmentChange("N");
					controlBlock.setPkAircraftChange("N");
					controlBlock.setPkRouteChange("N");
					controlBlock.setPkTransitionChange("N");

					// TODO first_record;

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWhenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWhenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWhenNewRecordInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWhenNewRecordInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(system.getCursorRecord(), "1")
					&& Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
				previousRecord("");

			}

			else {
				if (!Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)
						&& Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "I")) {

					// TODO validate_leg --- Program Unit Calling
					validateLeg("Y");

				}

				else {
					setItemProperty("control_block.std_leg_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

				}
				if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects
						.equals(rtrim(plStdStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent()), null)) {
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setRefInfo(coreptLib.getRefInfo(
							"PL_STD_STAR_LEG",
							toInteger(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())));
					if (!Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRefInfo(), null)) {
						setBlockItemProperty("pl_std_star_leg.fix_ident", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_std_star_leg.fix_icao_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_std_star_leg.fix_section_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_std_star_leg.fix_subsection_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);

					}

					else {
						setBlockItemProperty("pl_std_star_leg.fix_ident", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("pl_std_star_leg.fix_icao_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("pl_std_star_leg.fix_section_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("pl_std_star_leg.fix_subsection_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);

					}

				}

				if (Arrays.asList("INSERT", "NEW").contains(system.getRecordStatus())) {
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "CRE"));

				}

				else {
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

				}

			}

			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWhenValidateRecord(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWhenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			String query = null;
			Record rec = null;
			plStdStarLegWc1WhenValidateItem(reqDto);
			plStdStarLegWc2WhenValidateItem(reqDto);
			plStdStarLegWc3WhenValidateItem(reqDto);
			plStdStarLegWc4WhenValidateItem(reqDto);
			commonStdMagneticCourse();
			plStdStarLegRouteDistanceWhenValidateItem(reqDto);
			plStdStarLegRnpWhenValidateItem(reqDto);
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects.equals(system.getBlockStatus(), "QUERY")) {
				if (!Objects.equals(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), "-1"),
						"-1")) {
					if (!Arrays.asList("Y", "S", "H", "W", "N", "I", "O")
							.contains(nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "Y"))) {

						// TODO dsp_msg("Validate indicator can only 'Y','S','H','W','N' or 'I'");
						coreptLib.dspMsg("Validate indicator can only 'Y','S','H','W','N' or 'I'");
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(null);

					}

					if (Arrays.asList("I", "W", "N")
							.contains(plStdStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd())
							&& !Objects.equals(plStdStar.getRefInfo(), null)) {
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(null);

					}

					if (Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setFileRecno(new CustomInteger("0"));
						if (Objects.equals(
								rtrim(toString(
										plStdStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())),
								null)) {

							query = """
									select dcr_number_seq.nextval from dual
										""";
							rec = app.selectInto(query);
							plStdStarLeg.getRow(system.getCursorRecordIndex())
									.setCreateDcrNumber(new CustomInteger(toString(rec.getInt())));

						}

					}

					if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixMultipleCode(),
							null)
							&& !Objects.equals(
									nvl(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIdent(), "NOPT"),
									"NOPT")
							&& !Objects.equals(
									plStdStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(), "RF")) {
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setCenterFixMultipleCode("0");

					}

					// TODO validate_leg --- Program Unit Calling
					validateLeg("Y");
					controlBlock.setValidated("N");

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegKeyUp(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegKeyUp Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(), null)
					&& Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), null)
					&& Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber(), null)) {
				clearRecord("");
				if (Objects.equals(system.getLastRecord(), false)) {
					previousRecord("");

				}

			}

			else {
				previousRecord("");

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegKeyUp executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegKeyUp Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegKeyDown(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegKeyDown Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), null)
					&& Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(),
							null)) {
				// null;

			}

			else {
				controlBlock.setPreSeq(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum());
				controlBlock
						.setTransitionIdent(plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setRouteType(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setAircraftType(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType());
				system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
				plStdStarLeg.getData().add(system.getCursorRecordIndex(), new PlStdStarLeg());
				// nextRecord("");
				if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setRouteType(controlBlock.getRouteType());
					plStdStarLeg.getRow(system.getCursorRecordIndex())
							.setTransitionIdent(controlBlock.getTransitionIdent());
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setAircraftType(controlBlock.getAircraftType());
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(controlBlock.getPreSeq() + 10);

				}

				// goItem("pl_std_star_leg.fix_ident");

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegKeyDown executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegKeyDown Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegKeyCrerec(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegKeyCrerec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
					global.getDataSupplier(), toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
					parameter.getRecordType(), "CRE"));
			if (Objects.equals(parameter.getUpdRec(), "Y")) {
				if (Objects.equals(system.getCursorBlock(), "PL_STD_STAR_LEG")
						&& Objects.equals(system.getCursorRecordIndex(), 0)
						&& Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
					system.setCursorRecordIndex(0);
					if (!Objects.equals(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(), null)) {
						plStdStarLeg.getRow(system.getCursorRecordIndex())
								.setRouteType(plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setTransitionIdent(
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setAircraftType(
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(10);

					}

					else {

						message("please create the segment first.");
						throw new FormTriggerFailureException();

					}

				}

				else {
					controlBlock.setPreSeq(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum());
					controlBlock.setTransitionIdent(
							plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent());
					controlBlock.setRouteType(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType());
					controlBlock.setAircraftType(plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType());
					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
					plStdStarLeg.getData().add(system.getCursorRecordIndex(), new PlStdStarLeg());
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setRouteType(controlBlock.getRouteType());
					plStdStarLeg.getRow(system.getCursorRecordIndex())
							.setTransitionIdent(controlBlock.getTransitionIdent());
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setAircraftType(controlBlock.getAircraftType());
					plStdStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(-1);

					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
					if (!Objects.equals(controlBlock.getRouteType(),
							plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteType())
							|| !Objects.equals(controlBlock.getTransitionIdent(),
									plStdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())
							|| !Objects.equals(controlBlock.getAircraftType(),
									plStdStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType())
							|| Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(),
									null)) {
						system.setCursorRecordIndex(system.getCursorRecordIndex() - 1);
						plStdStarLeg.getRow(system.getCursorRecordIndex())
								.setSequenceNum(controlBlock.getPreSeq() + 10);

					}

					else {
						controlBlock.setNextSeq(plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum());
						system.setCursorRecordIndex(system.getCursorRecordIndex() - 1);
						plStdStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(
								Math.floorDiv((controlBlock.getPreSeq() + controlBlock.getNextSeq()), 2));

					}

				}
				plStdStarLeg.getRow(system.getCursorRecordIndex()).setRecordStatus("INSERT");
				plStdStarLeg.getRow(system.getCursorRecordIndex()).setAirportIdent(plStdStar.getAirportIdent());
				plStdStarLeg.getRow(system.getCursorRecordIndex()).setStarIdent(plStdStar.getStarIdent());
				plStdStarLeg.getRow(system.getCursorRecordIndex()).setProcessingCycle(plStdStar.getProcessingCycle());
				plStdStarLeg.getRow(system.getCursorRecordIndex()).setAirportIcao(plStdStar.getAirportIcao());
				plStdStarLeg.getRow(system.getCursorRecordIndex()).setDataSupplier(plStdStar.getDataSupplier());
				system.setFormStatus("CHANGED");
				system.setBlockStatus("CHANGED");
				system.setRecordStatus("INSERT");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegKeyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegKeyCrerec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegFixIdentOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegFixIdentOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_STD_STAR_LEG','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegFixIdentOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegFixIdentOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegFixIcaoCodeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegFixIcaoCodeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_STD_STAR_LEG','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegFixIcaoCodeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegFixIcaoCodeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegFixSectionCodeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegFixSectionCodeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_LEG", "C");
				refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_LEG", "C");
				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_STD_STAR_LEG','C')
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							null);

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegFixSectionCodeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegFixSectionCodeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegFixSubsectionCodeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegFixSubsectionCodeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				refreshMasterLibrary.checkReferenceInfo("PL_STD_STAR_LEG", "C");
				// vMessage =
				// refresh_master_library.check_reference_info('PL_STD_STAR_LEG','C');
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							null);

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegFixSubsectionCodeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegFixSubsectionCodeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWc1WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWc1WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWc1WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWc1WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWc2WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWc2WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWc2WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWc2WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWc3WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWc3WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			setWaypointCode();
			// TODO Set_waypoint_code --- Program Unit Calling
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWc3WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWc3WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegWc4WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegWc4WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegWc4WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegWc4WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegMagneticCourseWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegMagneticCourseWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			plStdStarLeg.getRow(system.getCursorRecordIndex()).setMagneticCourse(
					coreptLib.magcourseprc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse()));

			if (length(plStdStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse()) > 4) {

				coreptLib.dspMsg("Magnetic Course should not exceed 4 characters");
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegMagneticCourseWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegMagneticCourseWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	private void commonStdMagneticCourse() throws Exception {
		try {
			plStdStarLeg.getRow(system.getCursorRecordIndex()).setMagneticCourse(
					coreptLib.magcourseprc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse()));

			if (length(plStdStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse()) > 4) {

				coreptLib.dspMsg("Magnetic Course should not exceed 4 characters");
				throw new FormTriggerFailureException();

			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegRouteDistanceWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegRouteDistanceWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO
			plStdStarLeg.getRow(system.getCursorRecordIndex()).setRouteDistance(
					coreptLib.routedistanceprc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance()));

			// Route_Distance_Prc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance());
			if (length(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance()) > 4) {

				// TODO dsp_msg("Route Distance should not exceed 4 characters");
				coreptLib.dspMsg("Route Distance should not exceed 4 characters");
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegRouteDistanceWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegRouteDistanceWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegRnpWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegRnpWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO RNP_Prc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRnp());
			if (Objects.equals(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRnp(), "0")) {
				plStdStarLeg.getRow(system.getCursorRecordIndex())
						.setRnp(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRnp());
			} else {
				plStdStarLeg.getRow(system.getCursorRecordIndex())
						.setRnp(coreptLib.rnpprc(plStdStarLeg.getRow(system.getCursorRecordIndex()).getRnp()));
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegRnpWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegRnpWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegCloseDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("pl_std_cntd");
			goItem("pl_std_star_leg.se9uence_num");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plStdStarLegOpenDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plStdStarLegOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("pl_std_cntd");
			goItem("pl_std_star_leg.atc_ind");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plStdStarLegOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plStdStarLegOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	void plTldStarWhenNewRecordInstance() throws Exception {
		if (Objects.equals(system.getMode(), "NORMAL")) {
			if (!Objects.equals(plTldStar.getAirportIdent(), null)
					&& (Objects.equals(system.getFormStatus(), "CHANGED")
							|| !Objects.equals(nvl(plTldStar.getValidateInd(), "N"), "Y"))
					&& (Objects.equals(controlBlock.getValidated(), "N")
							|| !Objects.equals(controlBlock.getTempDcr(), toInteger(plTldStar.getCreateDcrNumber())))) {

				// TODO validate_procedure --- Program Unit Calling
				validateProcedure("Y");

			}

			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(plTldStar.getGeneratedInHouseFlag(), "N")) {
					setBlockItemProperty("pl_tld_star.processing_cycle", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setItemInstanceProperty("pl_tld_star.processing_cycle", "", "", "Not_updatable");
					setBlockItemProperty("pl_tld_star.generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setItemInstanceProperty("pl_tld_star.generated_in_house_flag", "", "", "Not_updatable");
					setBlockItemProperty("pl_tld_star.validate_ind", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);
					setItemInstanceProperty("pl_tld_star.validate_ind", "", "", "default");

				}

				else {
					if (Objects.equals(global.getLibRefreshed(), "Y")
							&& Objects.equals(plTldStar.getGeneratedInHouseFlag(), "Y")) {
						setBlockItemProperty("pl_tld_star.processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemInstanceProperty("pl_tld_star.processing_cycle", "", "", "Not_updatable");

					}

					else {
						setBlockItemProperty("pl_tld_star.processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemInstanceProperty("pl_tld_star.processing_cycle", "", "", "default");

					}
					setBlockItemProperty("pl_tld_star.generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);
					setItemInstanceProperty("pl_tld_star.generated_in_house_flag", "", "", "default");
					setBlockItemProperty("pl_tld_star.validate_ind", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("pl_tld_star_leg.validate_ind", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setItemInstanceProperty("pl_tld_star.validate_ind", "", "", "Not_updatable");

				}
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

			}

//			 TODO if_from_leg_search(2);
//			coreptLib.iffromlegsearch(2);
			controlBlock.setTempDcr(toInteger(nvl(plTldStar.getCreateDcrNumber(), 0)));
			if (!Objects.equals(controlBlock.getTldOverrideErrors(), null)) {
				if (Objects.equals(getItemProperty("CONTROL_BLOCK.TLD_OVERIDE", FormConstant.ENABLED), "FALSE")) {
					setBlockItemProperty("control_block.tld_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_TRUE);
					setBlockItemProperty("control_block.tld_overide", FormConstant.ENABLED, FormConstant.PROPERTY_TRUE);

				}

			}

			else {
				setBlockItemProperty("control_block.tld_overide", FormConstant.ENABLED, FormConstant.PROPERTY_FALSE);
				setBlockItemProperty("control_block.tld_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

			}

		}

		mergeDelete();
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarWhenNewRecordInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarWhenNewRecordInstance Executing");

		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(system.getMode(), "NORMAL")) {
				if (!Objects.equals(plTldStar.getAirportIdent(), null)
						&& (Objects.equals(system.getFormStatus(), "CHANGED")
								|| !Objects.equals(nvl(plTldStar.getValidateInd(), "N"), "Y"))
						&& (Objects.equals(controlBlock.getValidated(), "N") || !Objects
								.equals(controlBlock.getTempDcr(), toInteger(plTldStar.getCreateDcrNumber())))) {

					// TODO validate_procedure --- Program Unit Calling
					validateProcedure("Y");

				}

				if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
					if (Objects.equals(plTldStar.getGeneratedInHouseFlag(), "N")) {
						setBlockItemProperty("pl_tld_star.processing_cycle", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemInstanceProperty("pl_tld_star.processing_cycle", "", "", "Not_updatable");
						setBlockItemProperty("pl_tld_star.generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemInstanceProperty("pl_tld_star.generated_in_house_flag", "", "", "Not_updatable");
						setBlockItemProperty("pl_tld_star.validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemInstanceProperty("pl_tld_star.validate_ind", "", "", "default");

					}

					else {
						if (Objects.equals(global.getLibRefreshed(), "Y")
								&& Objects.equals(plTldStar.getGeneratedInHouseFlag(), "Y")) {
							setBlockItemProperty("pl_tld_star.processing_cycle", FormConstant.UPDATE_ALLOWED,
									FormConstant.PROPERTY_FALSE);
							setItemInstanceProperty("pl_tld_star.processing_cycle", "", "", "Not_updatable");

						}

						else {
							setBlockItemProperty("pl_tld_star.processing_cycle", FormConstant.UPDATE_ALLOWED,
									FormConstant.PROPERTY_TRUE);
							setItemInstanceProperty("pl_tld_star.processing_cycle", "", "", "default");

						}
						setBlockItemProperty("pl_tld_star.generated_in_house_flag", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setItemInstanceProperty("pl_tld_star.generated_in_house_flag", "", "", "default");
						setBlockItemProperty("pl_tld_star.validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_tld_star_leg.validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setItemInstanceProperty("pl_tld_star.validate_ind", "", "", "Not_updatable");

					}
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

				}

				// TODO if_from_leg_search(2);
				coreptLib.iffromlegsearch(2);
				controlBlock.setTempDcr(toInteger(nvl(plTldStar.getCreateDcrNumber(), 0)));
				if (!Objects.equals(controlBlock.getTldOverrideErrors(), null)) {
					if (Objects.equals(getItemProperty("CONTROL_BLOCK.TLD_OVERIDE", FormConstant.ENABLED), "FALSE")) {
						setBlockItemProperty("control_block.tld_overide", FormConstant.VISIBLE,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("control_block.tld_overide", FormConstant.ENABLED,
								FormConstant.PROPERTY_TRUE);

					}

				}

				else {
					setBlockItemProperty("control_block.tld_overide", FormConstant.ENABLED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("control_block.tld_overide", FormConstant.VISIBLE,
							FormConstant.PROPERTY_FALSE);

				}

			}

			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void plTldStarPreQuery(AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarPreQuery Executing");

		try {
			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				plTldStar.setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plTldStar.setProcessingCycle(global.getProcessingCycle());

				}

			}

			log.info(" plTldStarPreQuery executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarPreQuery Service");
			throw e;
		}
	}

	@Override
	public void plTldStarPostQuery() throws Exception {
		log.info(" plTldStarPostQuery Executing");
		// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
		// BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				plTldStar.setOldProcessingCycle(toInteger(plTldStar.getProcessingCycle()));
				plTldStar.setOldGInHouse(plTldStar.getGeneratedInHouseFlag());
				plTldStar.setOldValidateInd(plTldStar.getValidateInd());
				// TODO
				plTldStar.setRefInfo(coreptLib.getRefInfo("PL_TLD_STAR", toInteger(plTldStar.getCreateDcrNumber())));
				controlBlock.setValidated("N");
				controlBlock.setTldValidationErrors(null);

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarPostQuery executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarWhenValidateRecord(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarWhenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
//			pltldcustomerIdentWhenvalidateIt();
//			pltldprocessingcyclewhenvalidateit();
//			plTldStarAirportIcaoWhenValidateIt();
//			plTldStarStarIdentWhenValidateIt();
//			plTldStarSpecialsIndWhenValidateIt();
//			plTldStarGeneratedInHouseFlagWhenValidateIt();

			OracleHelpers.bulkClassMapper(reqDto, this);
			String query = null;
			Record rec = null;
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects.equals(system.getRecordStatus(), "QUERY")
					&& Objects.equals(system.getCursorBlock(), "PL_TLD_STAR")) {
				if (!Arrays.asList("Y", "S", "H", "W", "N", "I", "O").contains(nvl(plTldStar.getValidateInd(), "Y"))
						|| (Arrays.asList("Y", "S", "H", "W", "N", "O")
								.contains(nvl(plTldStar.getOldValidateInd(), "Y"))
								&& !Arrays.asList("Y", "S", "H", "W", "N", "O")
										.contains(nvl(plTldStar.getValidateInd(), "Y")))) {

					coreptLib.dspMsg("You can only update the validate_ind to 'Y','S','H','W' or 'N'");
					plTldStar.setValidateInd(plTldStar.getOldValidateInd());

				}

				if (Objects.equals(plTldStar.getValidateInd(), null)
						&& !Objects.equals(plTldStar.getOldValidateInd(), null)) {
					plTldStar.setValidateInd(plTldStar.getOldValidateInd());

				}

				if (!Objects.equals(plTldStar.getRefInfo(), null)
						&& Arrays.asList("W", "N", "I").contains(plTldStar.getValidateInd())) {
					controlBlock.setTldValidationErrors(plTldStar.getRefInfo());
					setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
							FormConstant.PROPERTY_TRUE);
					setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
							FormConstant.PROPERTY_TRUE);
					plTldStar.setValidateInd(plTldStar.getOldValidateInd());

				}

				coreptLib.validateextrafields(toInteger(plTldStar.getProcessingCycle()),
						plTldStar.getGeneratedInHouseFlag());
				if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					if (!Objects.equals(plTldStar.getProcessingCycle(), toString(plTldStar.getOldProcessingCycle()))) {
						if (Objects.equals(checkProcPk(), false)) {

							coreptLib.dspMsg("This change will generate a duplicate record! ");
							plTldStar.setProcessingCycle(toString(plTldStar.getOldProcessingCycle()));

						}

						else if (!Objects.equals(plTldStar.getRefInfo(), null)) {
							controlBlock.setTldValidationErrors(plTldStar.getRefInfo());
							setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
									FormConstant.PROPERTY_TRUE);
							setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
									FormConstant.PROPERTY_TRUE);
							plTldStar.setProcessingCycle(toString(plTldStar.getOldProcessingCycle()));

						}

					}

				}

				else {
					if (Objects.equals(checkProcPk(), false)) {

						coreptLib.dspMsg("This record already exists!");
						throw new FormTriggerFailureException();

					}

					plTldStar.setDataSupplier(global.getDataSupplier());
					if (Objects.equals(plTldStar.getCreateDcrNumber(), null)) {

						query = """
								select dcr_number_seq.nextval from dual
								""";
						rec = app.selectInto(query);
						plTldStar.setCreateDcrNumber(new CustomInteger(toString(rec.getInt())));
						controlBlock.setTempDcr(toInteger(plTldStar.getCreateDcrNumber()));

					}

					plTldStar.setUpdateDcrNumber(new CustomInteger(global.getDcrNumber()));
					plTldStar.setValidateInd("I");
					plTldStar.setOldValidateInd("I");
					controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);

				}
				controlBlock.setValidated("N");

				// TODO validate_keys --- Program Unit Calling
				validateKeys("Y");
				if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					if (!Objects.equals(controlBlock.getTldValidationErrors(), null)
							&& !Objects.equals(plTldStar.getRefInfo(), null)) {
						plTldStar.setGeneratedInHouseFlag(plTldStar.getOldGInHouse());

					}

					else {
						if (!Objects.equals(plTldStar.getOldGInHouse(), plTldStar.getGeneratedInHouseFlag()) || !Objects
								.equals(plTldStar.getProcessingCycle(), toString(plTldStar.getOldProcessingCycle()))) {
							plTldStar.setOldProcessingCycle(toInteger(plTldStar.getProcessingCycle()));
							plTldStar.setOldGInHouse(plTldStar.getGeneratedInHouseFlag());
							plTldStar.setFlagChange("Y");

						}

					}

				}

				if (Objects.equals(global.getLibRefreshed(), "Y") && (Objects.equals(plTldStar.getOldValidateInd(), "Y")
						|| Objects.equals(plTldStar.getValidateInd(), "Y"))) {
					if (Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle())
							.contains(toChar(plTldStar.getProcessingCycle()))) {

						refreshMasterLibrary.setRecordGroup(toInteger(plTldStar.getCreateDcrNumber()),
								plTldStar.getValidateInd(), "PL_TLD_STAR", toInteger(plTldStar.getProcessingCycle()),
								"U");

					}

				}
				
				for (PlTldStarSegment plTldStarSegment : plTldStarSegment.getData()) {
					if(!Objects.equals(plTldStarSegment.getProcessingCycle(), plTldStar.getProcessingCycle())) {
						plTldStarSegment.setProcessingCycle(plTldStar.getProcessingCycle());
					}
				}
				
				for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
					if(!Objects.equals(plTldStarLeg.getProcessingCycle(), plTldStar.getProcessingCycle())) {
						plTldStarLeg.setProcessingCycle(plTldStar.getProcessingCycle());
					}
				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarPostDelete(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarPostDelete Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldStar.getValidateInd(), "I")) {
				controlBlock.setCountInvalid(controlBlock.getCountInvalid() - 1);

			}

			setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarPostDelete executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarPostDelete Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarOnPopulateDetails(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarOnPopulateDetails Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// String recstat = system.getRecordStatus();
			String startitm = system.getCursorItem();
			Object relId = null;

			// coverity-fixes
			// if ((Objects.equals(recstat, "NEW") || Objects.equals(recstat, "INSERT"))) {
			//
			// // return
			// }

			if (((!Objects.equals(plTldStar.getAirportIdent(), null))
					|| (!Objects.equals(plTldStar.getAirportIcao(), null))
					|| (!Objects.equals(plTldStar.getStarIdent(), null))
					|| (!Objects.equals(plTldStar.getDataSupplier(), null))
					|| (!Objects.equals(plTldStar.getProcessingCycle(), null))
					|| (!Objects.equals(plTldStar.getCustomerIdent(), null)))) {
				// TODO relId = findRelation("PL_TLD_STAR.PL_TLD_STAR_PL_TLD_STAR_SE");

				// TODO Query_Master_Details(rel_id,'PL_TLD_STAR_SEGMENT') --- Program Unit
				// Calling
				queryMasterDetails(relId, "PL_TLD_STAR_SEGMENT");

			}

			if (((!Objects.equals(plTldStar.getAirportIdent(), null))
					|| (!Objects.equals(plTldStar.getAirportIcao(), null))
					|| (!Objects.equals(plTldStar.getStarIdent(), null))
					|| (!Objects.equals(plTldStar.getDataSupplier(), null))
					|| (!Objects.equals(plTldStar.getProcessingCycle(), null))
					|| (!Objects.equals(plTldStar.getCustomerIdent(), null)))) {
				// TODO relId = findRelation("PL_TLD_STAR.PL_TLD_STAR_PL_TLD_STAR_LEG");

				// TODO Query_Master_Details(rel_id,'PL_TLD_STAR_LEG') --- Program Unit Calling
				queryMasterDetails(relId, "PL_TLD_STAR_LEG");

			}

			if ((!Objects.equals(system.getCursorItem(), startitm))) {
				// goItem(startitm);

				// TODO Check_Package_Failure --- Program Unit Calling
				checkPackageFailure();

			}

			if (Objects.equals(plTldStar.getValidateInd(), "I")) {
				setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_TRUE);
				setItemProperty("control_block.tld_validation_errors", FormConstant.ENABLED,
						FormConstant.PROPERTY_TRUE);

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarOnPopulateDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarOnPopulateDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void pltldcustomerIdentWhenvalidateIt() throws Exception {
		try {
			if (instr(plTldStar.getCustomerIdent(), "%") > 0 || instr(plTldStar.getCustomerIdent(), "_") > 0) {

				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");
				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				plTldStar.setCustomerIdent(null);
				throw new FormTriggerFailureException();

			}

			Integer vFlag = 0;
			// Integer vAllowUpdate = 0;
			String vBlockName = system.getCursorBlock();

			// vAllowUpdate = 0;
			if (Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
				if (Objects.equals(parameter.getRecordType(), "T")) {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(global.getProcessingCycle()), global.getDataSupplier(),
								parameter.getRecordType(), toString(nameIn(this, vBlockName + ".customer_ident")));

					}

					else {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(nameIn(this, vBlockName + ".processing_cycle")), global.getDataSupplier(),
								parameter.getRecordType(), toString(nameIn(this, vBlockName + ".customer_ident")));

					}

				}

				else {
					if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(global.getProcessingCycle()), global.getDataSupplier(),
								parameter.getRecordType(), null);

					}

					else {
						vFlag = coreptLib.validateCustomer(toInteger(global.getDcrNumber()),
								toInteger(nameIn(this, vBlockName + ".processing_cycle")), global.getDataSupplier(),
								parameter.getRecordType(), null);

					}

				}
				if (Arrays.asList("J", "L", "E").contains(global.getDataSupplier())) {
					if (Arrays.asList(6, 4, 3, 2, 1).contains(vFlag)) {
						// vAllowUpdate = 1;

						plTldStar.setProcessingCycle(global.getProcessingCycle());

						parameter.setCompCycle(toInteger(getCycle()));

					}

					else if (Objects.equals(vFlag, 0)) {
						if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {

							coreptLib.dspMsg("Customer is not associated with DCR# " + global.getDcrNumber()
									+ " Or with\nprocessing cycle " + global.getProcessingCycle());

						}

						else {

							coreptLib.dspMsg("Customer is not associated with DCR# " + global.getDcrNumber()
									+ " Or with\nprocessing cycle " + nameIn(this, vBlockName + ".processing_cycle"));

						}
						throw new FormTriggerFailureException();

					}

				}

				else if (Arrays.asList("Q", "N").contains(global.getDataSupplier())) {
					if (Objects.equals(vFlag, 5)) {
						// vAllowUpdate = 1;

						plTldStar.setProcessingCycle(global.getProcessingCycle());

						parameter.setCompCycle(toInteger(getCycle()));

					}

				}

			}
			log.info(" plTldStarCustomerIdentWhenValidateItem executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarCustomerIdentWhenValidateItem Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarCustomerIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarCustomerIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			pltldcustomerIdentWhenvalidateIt();

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarCustomerIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarCustomerIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void pltldprocessingcyclewhenvalidateit() throws Exception {
		try {
			String lsReturn = null;

			if (Objects.equals(rtrim(toString(plTldStar.getProcessingCycle())), null)) {
				plTldStar.setProcessingCycle(global.getProcessingCycle());

			}

			else {
				lsReturn = coreptLib.dcrEffectiveCycleFun(toInteger(plTldStar.getProcessingCycle()), null);
				if (Objects.equals(lsReturn, "N")) {
					throw new FormTriggerFailureException();

				}

			}
			log.info(" plTldStarProcessingCycleWhenValidateItem executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarProcessingCycleWhenValidateItem Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarProcessingCycleWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarProcessingCycleWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			pltldprocessingcyclewhenvalidateit();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarProcessingCycleWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarProcessingCycleWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarAirportIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarAirportIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (instr(plTldStar.getAirportIdent(), "%") > 0 || instr(plTldStar.getAirportIdent(), "_") > 0) {
				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");

				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				plTldStar.setAirportIdent(null);
				throw new FormTriggerFailureException();

			}

			String lsIcao = null;

			lsIcao = coreptLib.populateAirportIcao(rtrim(plTldStar.getAirportIdent()),
					rtrim(plTldStar.getAirportIcao()), plTldStar.getCustomerIdent(),
					toInteger(plTldStar.getProcessingCycle()), nvl(plTldStar.getGeneratedInHouseFlag(), "Y"));
			if (Objects.equals(lsIcao, "--")) {
				plTldStar.setAirportIcao(null);

			}

			else {
				plTldStar.setAirportIcao(lsIcao);

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarAirportIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarAirportIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void plTldStarAirportIcaoWhenValidateIt() throws Exception {
		try {
			if (instr(plTldStar.getAirportIcao(), "%") > 0 || instr(plTldStar.getAirportIcao(), "_") > 0) {

				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");
				plTldStar.setAirportIcao(null);
				throw new FormTriggerFailureException();

			}

			log.info(" plTldStarAirportIcaoWhenValidateItem executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarAirportIcaoWhenValidateItem Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarAirportIcaoWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarAirportIcaoWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			plTldStarAirportIcaoWhenValidateIt();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarAirportIcaoWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarAirportIcaoWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void plTldStarStarIdentWhenValidateIt() throws Exception {
		try {
			if (instr(plTldStar.getStarIdent(), "%") > 0 || instr(plTldStar.getStarIdent(), "_") > 0) {
				coreptLib.dspMsg("Wildcard '%' and/or '_' is not allowed.");
				// TODO dsp_msg("Wildcard '%' and/or '_' is not allowed.");
				plTldStar.setStarIdent(null);
				throw new FormTriggerFailureException();

			}

			log.info(" plTldStarStarIdentWhenValidateItem executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarStarIdentWhenValidateItem Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarStarIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarStarIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			plTldStarStarIdentWhenValidateIt();

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarStarIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarStarIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void plTldStarSpecialsIndWhenValidateIt() {
		try {
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(rtrim(plTldStar.getSpecialsInd()), null)) {
					plTldStar.setSpecialsInd("N");

				}

			}

			log.info(" plTldStarSpecialsIndWhenValidateItem executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSpecialsIndWhenValidateItem Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSpecialsIndWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSpecialsIndWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			plTldStarSpecialsIndWhenValidateIt();

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSpecialsIndWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSpecialsIndWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public void plTldStarGeneratedInHouseFlagWhenValidateIt() {
		try {
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(plTldStar.getGeneratedInHouseFlag(), null)) {
					plTldStar.setGeneratedInHouseFlag("Y");

				}

			}

			log.info(" plTldStarGeneratedInHouseFlagWhenValidateItem executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarGeneratedInHouseFlagWhenValidateItem Service");
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarGeneratedInHouseFlagWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarGeneratedInHouseFlagWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			plTldStarGeneratedInHouseFlagWhenValidateIt();

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarGeneratedInHouseFlagWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarGeneratedInHouseFlagWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarValidateIndPreTextItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarValidateIndPreTextItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && Objects.equals(plTldStar.getValidateInd(), "I")
					&& Objects.equals(plTldStar.getGeneratedInHouseFlag(), "N")) {
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarValidateIndPreTextItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarValidateIndPreTextItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentWhenNewRecordInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentWhenNewRecordInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")
					&& !Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(), null)) {
				if (Objects.equals(refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT", "W"), "N")) {
					setBlockItemProperty("pl_tld_star_segment.route_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("pl_tld_star_segment.transition_ident", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);
					setBlockItemProperty("pl_tld_star_segment.aircraft_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_FALSE);

				}

				else {
					setBlockItemProperty("pl_tld_star_segment.route_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);
					setBlockItemProperty("pl_tld_star_segment.transition_ident", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);
					setBlockItemProperty("pl_tld_star_segment.aircraft_type", FormConstant.UPDATE_ALLOWED,
							FormConstant.PROPERTY_TRUE);

				}
				if (Arrays.asList("INSERT", "NEW").contains(system.getRecordStatus())) {
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "CRE"));

				}

				else {
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

				}

			}

			if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {

				for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
					if (Objects.equals(plTldStarLeg.getRouteType(), controlBlock.getOldRouteType())
							&& Objects.equals(plTldStarLeg.getTransitionIdent(), controlBlock.getOldTransitionIdent())
							&& Objects.equals(plTldStarLeg.getAircraftType(), controlBlock.getOldAircraftType())) {
						plTldStarLeg.setRouteType(controlBlock.getNewRouteType());
						plTldStarLeg.setTransitionIdent(controlBlock.getNewTransitionIdent());
						plTldStarLeg.setAircraftType(controlBlock.getNewAircraftType());

					}

				}

				controlBlock.setPkSegmentChange("N");
				controlBlock.setPkAircraftChange("N");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkTransitionChange("N");

			}

			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentWhenValidateRecord(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentWhenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			plTldStarSegmentRouteTypeWhenValidateItem(reqDto);
			plTldStarSegmentTransitionIdentWhenValidateItem(reqDto);
			plTldStarSegmentQualifier1WhenValidateItem(reqDto);
			plTldStarSegmentQualifier2WhenValidateItem(reqDto);
			plTldStarSegmentAircraftTypeWhenValidateItem(reqDto);
			plTldStarSegmentProcDesignMagVarIndWhenValidateItem(reqDto);
			plTldStarSegmentCreateDcrNumberWhenValidateItem(reqDto);
			OracleHelpers.bulkClassMapper(reqDto, this);
			String query = null;
			Record rec = null;
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects.equals(system.getBlockStatus(), "QUERY")) {
				if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {
					if (Objects.equals(checkSegmentPk(), false)) {

						coreptLib.dspMsg("This change will generate a duplicate record! ");
						plTldStarSegment.getRow(system.getCursorRecordIndex())
								.setRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
						plTldStarSegment.getRow(system.getCursorRecordIndex()).setTransitionIdent(
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
						plTldStarSegment.getRow(system.getCursorRecordIndex()).setAircraftType(
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
						controlBlock.setPkSegmentChange("N");
						controlBlock.setPkAircraftChange("N");
						controlBlock.setPkRouteChange("N");
						controlBlock.setPkTransitionChange("N");

					}

				}

				if (Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getCreateDcrNumber(), null)) {

					query = """
									select dcr_number_seq.nextval from dual
							""";
					rec = app.selectInto(query);
					plTldStarSegment.getRow(system.getCursorRecordIndex())
							.setCreateDcrNumber(new CustomInteger(toString(rec.getInt())));
					plTldStarSegment.getRow(system.getCursorRecordIndex())
							.setGeneratedInHouseFlag(plTldStar.getGeneratedInHouseFlag());
					plTldStarSegment.getRow(system.getCursorRecordIndex())
							.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
					plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
							plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
					plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldAircraftType(
							plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
					controlBlock.setPkSegmentChange("N");
					controlBlock.setPkAircraftChange("N");
					controlBlock.setPkRouteChange("N");
					controlBlock.setPkTransitionChange("N");

				}

				controlBlock.setValidated("N");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void plTldStarSegmentPostQuery() throws Exception {
		log.info(" plTldStarSegmentPostQuery Executing");

		try {
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				for (PlTldStarSegment plTldStarSegment : plTldStarSegment.getData()) {
					plTldStarSegment.setOldRouteType(plTldStarSegment.getRouteType());
					plTldStarSegment.setOldTransitionIdent(plTldStarSegment.getTransitionIdent());
					plTldStarSegment.setOldAircraftType(plTldStarSegment.getAircraftType());
				}
			}

			log.info(" plTldStarSegmentPostQuery executed successfully");

		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentPostQuery Service");
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentKeyCrerec(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentKeyCrerec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			// String lcCurRec = null;

			parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
					global.getDataSupplier(), toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
					parameter.getRecordType(), "CRE"));
			if (Objects.equals(parameter.getUpdRec(), "Y")) {
				if (Objects.equals(controlBlock.getPkSegmentChange(), "N")
						&& Objects.equals(system.getCursorBlock(), "PL_TLD_STAR_SEGMENT")
						&& (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())
								|| !Objects.equals(
										plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
										plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())
								|| !Objects.equals(
										plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
										plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType()))) {
					// lcCurRec = system.getCursorRecord();

					for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
						if (Objects.equals(plTldStarLeg.getRouteType(), controlBlock.getOldRouteType())
								&& Objects.equals(plTldStarLeg.getTransitionIdent(),
										controlBlock.getOldTransitionIdent())
								&& Objects.equals(plTldStarLeg.getAircraftType(), controlBlock.getOldAircraftType())) {
							plTldStarLeg.setRouteType(controlBlock.getNewRouteType());
							plTldStarLeg.setTransitionIdent(controlBlock.getNewTransitionIdent());
							plTldStarLeg.setAircraftType(controlBlock.getNewAircraftType());

						}

					}

					// TODO first_record;
					controlBlock.setPkSegmentChange("N");
					controlBlock.setPkRouteChange("N");
					controlBlock.setPkTransitionChange("N");
					controlBlock.setPkAircraftChange("N");

					// TODO go_record(lc_cur_rec);

				}

				// createRecord("");
				PlTldStarSegment seg = new PlTldStarSegment();
				seg.setAircraftType("-");
				plTldStarSegment.getData().add(system.getCursorRecordIndex() + 1, seg);
				plTldStarSegment.getRow(system.getCursorRecordIndex() + 1).setRecordStatus("NEW");
				system.setFormStatus("CHANGED");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentKeyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentKeyCrerec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentRouteTypeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentRouteTypeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT", "C");
				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_TLD_STAR_SEGMENT','C')
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					// TODO
					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							toString(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentRouteTypeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentRouteTypeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentRouteTypeWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentRouteTypeWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())
					&& (Objects.equals(controlBlock.getPkAircraftChange(), "Y")
							|| Objects.equals(controlBlock.getPkTransitionChange(), "Y"))) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setPkRouteChange("Y");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("N");
				controlBlock.setPkSegmentChange("Y");

			}

			else if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkRouteChange("Y");
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkAircraftChange("N");
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentRouteTypeWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentRouteTypeWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentTransitionIdentOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentTransitionIdentOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;

				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT", "C");
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							toString(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentTransitionIdentOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentTransitionIdentOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentTransitionIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentTransitionIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())
					&& (Objects.equals(controlBlock.getPkRouteChange(), "Y")
							|| Objects.equals(controlBlock.getPkAircraftChange(), "Y"))) {
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkAircraftChange("N");
				controlBlock.setPkTransitionChange("Y");
				controlBlock.setPkSegmentChange("Y");

			}

			else if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkTransitionChange("Y");
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentTransitionIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentTransitionIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentTransitionIdentWhenMouseDoubleclick(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentTransitionIdentWhenMouseDoubleclick Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("pl_tld_star_leg.transition_ident");

			// TODO first_record;
			while (true) {
				if (Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {

					// TODO next_item;
					break;

				}

				else {
					nextRecord("");

				}
				break;

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentTransitionIdentWhenMouseDoubleclick executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentTransitionIdentWhenMouseDoubleclick Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentQualifier1OnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentQualifier1OnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;

				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_TLD_STAR_SEGMENT','C')
				// TODO parameter.setUpdRec(setActionRestr(nameIn(this,
				// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					// TODO
					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							toString(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentQualifier1OnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentQualifier1OnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentQualifier1WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentQualifier1WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentQualifier1WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentQualifier1WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentQualifier2OnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentQualifier2OnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT", "C");
				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_TLD_STAR_SEGMENT','C')
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							toString(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentQualifier2OnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentQualifier2OnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentQualifier2WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentQualifier2WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentQualifier2WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentQualifier2WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentAircraftTypeWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentAircraftTypeWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())
					&& (Objects.equals(controlBlock.getPkTransitionChange(), "Y")
							|| Objects.equals(controlBlock.getPkRouteChange(), "Y"))) {
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkAircraftChange("Y");
				controlBlock.setPkSegmentChange("Y");

			}

			else if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkAircraftChange("Y");
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentAircraftTypeWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentAircraftTypeWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentAircraftTypeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentAircraftTypeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT", "C");
				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_TLD_STAR_SEGMENT','C')
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {

					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							toString(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentAircraftTypeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentAircraftTypeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentProcDesignMagVarIndWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentProcDesignMagVarIndWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())
					&& (Objects.equals(controlBlock.getPkTransitionChange(), "Y")
							|| Objects.equals(controlBlock.getPkRouteChange(), "Y"))) {
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkTransitionChange("N");
				controlBlock.setPkRouteChange("N");
				controlBlock.setPkAircraftChange("Y");
				controlBlock.setPkSegmentChange("Y");

			}

			else if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkAircraftChange("Y");
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentProcDesignMagVarIndWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentProcDesignMagVarIndWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentProcDesignMagVarIndOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentProcDesignMagVarIndOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(global.getErrorCode(), 40200)) {
				// String vMessage = null;
				// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT",
				// "C");
				refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_SEGMENT", "C");
				parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

				// TODO v_message :=
				// refresh_master_library.check_reference_info('PL_TLD_STAR_SEGMENT','C')
				// TODO parameter.setUpdRec(setActionRestr(nameIn(this,
				// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
				if (Objects.equals(parameter.getUpdRec(), "N")) {
					coreptLib.dspActionMsg("U", parameter.getRecordType(), toInteger(nameIn(this, "global.dcr_number")),
							toInteger(nvl(nameIn(this, system.getCursorBlock() + ".processing_cycle"),
									global.getProcessingCycle())),
							toString(nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")));

					// TODO
					// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentProcDesignMagVarIndOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentProcDesignMagVarIndOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentCreateDcrNumberOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentCreateDcrNumberOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_TLD_STAR_SEGMENT','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentCreateDcrNumberOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentCreateDcrNumberOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarSegmentCreateDcrNumberWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarSegmentCreateDcrNumberWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent(),
					plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {
				controlBlock.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldRouteType());
				controlBlock.setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldTransitionIdent());
				controlBlock.setOldAircraftType(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getOldAircraftType());
				controlBlock.setNewRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setNewTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock
						.setNewAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
				plTldStarSegment.getRow(system.getCursorRecordIndex()).setOldTransitionIdent(
						plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				plTldStarSegment.getRow(system.getCursorRecordIndex())
						.setOldAircraftType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
				controlBlock.setPkSegmentChange("Y");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarSegmentCreateDcrNumberWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarSegmentCreateDcrNumberWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void plTldStarLegPostQuery() throws Exception {
		log.info(" plTldStarLegPostQuery Executing");

		try {
			for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
				if (!Objects.equals(plTldStarLeg.getWaypointDescCode(), null)) {
					plTldStarLeg.setWc1(substr(rpad(plTldStarLeg.getWaypointDescCode(), 4), 1, 1).trim());
					plTldStarLeg.setWc2(substr(rpad(plTldStarLeg.getWaypointDescCode(), 4), 2, 1).trim());
					plTldStarLeg.setWc3(substr(rpad(plTldStarLeg.getWaypointDescCode(), 4), 3, 1).trim());
					plTldStarLeg.setWc4(substr(rpad(plTldStarLeg.getWaypointDescCode(), 4), 4, 1).trim());
				}
			}

			log.info(" plTldStarLegPostQuery executed successfully");

		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegPostQuery Service");
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWhenNewBlockInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWhenNewBlockInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(controlBlock.getPkSegmentChange(), "Y")) {

					// TODO first_record;
					for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
						if (Objects.equals(plTldStarLeg.getRouteType(), controlBlock.getOldRouteType())
								&& Objects.equals(plTldStarLeg.getTransitionIdent(),
										controlBlock.getOldTransitionIdent())
								&& Objects.equals(plTldStarLeg.getAircraftType(), controlBlock.getOldAircraftType())) {
							plTldStarLeg.setRouteType(controlBlock.getNewRouteType());
							plTldStarLeg.setTransitionIdent(controlBlock.getNewTransitionIdent());
							plTldStarLeg.setAircraftType(controlBlock.getNewAircraftType());
							if (Objects.equals("QUERIED", plTldStarLeg.getRecordStatus())) {

								plTldStarLeg.setRecordStatus("CHANGED");
							}

						}

					}
					controlBlock.setPkSegmentChange("N");
					controlBlock.setPkAircraftChange("N");
					controlBlock.setPkRouteChange("N");
					controlBlock.setPkTransitionChange("N");

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWhenNewBlockInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWhenNewBlockInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWhenNewRecordInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWhenNewRecordInstance Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			 OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(system.getCursorRecord(), "1")
					&& Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
				previousRecord("");

			}

			else {
				if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)
						&& Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "I")) {

					// TODO validate_leg --- Program Unit Calling
					validateLeg("Y");

				}

				else {
					setItemProperty("control_block.tld_leg_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

				}
				if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects
						.equals(rtrim(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent()), null)) {
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setRefInfo(coreptLib.getRefInfo(
							"PL_TLD_STAR_LEG",
							toInteger(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())));
					if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRefInfo(), null)) {
						setBlockItemProperty("pl_tld_star_leg.fix_ident", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_tld_star_leg.fix_icao_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_tld_star_leg.fix_section_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);
						setBlockItemProperty("pl_tld_star_leg.fix_subsection_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);

					}

					else {
						setBlockItemProperty("pl_tld_star_leg.fix_ident", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("pl_tld_star_leg.fix_icao_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("pl_tld_star_leg.fix_section_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);
						setBlockItemProperty("pl_tld_star_leg.fix_subsection_code", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);

					}
					if (Objects.equals(plTldStar.getGeneratedInHouseFlag(), "N")) {
						setBlockItemProperty("pl_tld_star_leg.validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_TRUE);

					}

					else {
						setBlockItemProperty("pl_tld_star_leg.validate_ind", FormConstant.UPDATE_ALLOWED,
								FormConstant.PROPERTY_FALSE);

					}

				}

				if (Arrays.asList("INSERT", "NEW").contains(system.getRecordStatus())) {
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "CRE"));

				}

				else {
					parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));

				}

			}

			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWhenNewRecordInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWhenNewRecordInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWhenValidateRecord(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWhenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();

		try {
			plTldStarLegWc1WhenValidateItem(reqDto);
			plTldStarLegWc2WhenValidateItem(reqDto);
			plTldStarLegWc3WhenValidateItem(reqDto);
			plTldStarLegWc4WhenValidateItem(reqDto);
			commonMagneticCourse();
			plTldStarLegRouteDistanceWhenValidateItem(reqDto);
			plTldStarLegRnpWhenValidateItem(reqDto);

			OracleHelpers.bulkClassMapper(reqDto, this);
			if (!Objects.equals(parameter.getWorkType(), "VIEW") && !Objects.equals(system.getBlockStatus(), "QUERY")) {
				if (!Objects.equals(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), "-1"),
						"-1")) {
					if (!Arrays.asList("Y", "S", "H", "W", "N", "I", "O")
							.contains(nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd(), "Y"))) {

						coreptLib.dspMsg("Validate indicator can only 'Y','S','H','W','N' or 'I'");
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(null);

					}

					if (Arrays.asList("I", "W", "N")
							.contains(plTldStarLeg.getRow(system.getCursorRecordIndex()).getValidateInd())
							&& !Objects.equals(plTldStar.getRefInfo(), null)) {
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setValidateInd(null);

					}

					if (Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setFileRecno(new CustomInteger("0"));
						plTldStarLeg.getRow(system.getCursorRecordIndex())
								.setGeneratedInHouseFlag(plTldStar.getGeneratedInHouseFlag());
						if (Objects.equals(
								rtrim(toString(
										plTldStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber())),
								null)) {

							String query = """
									select dcr_number_seq.nextval from dual
										""";
							Record rec = app.selectInto(query);
							plTldStarLeg.getRow(system.getCursorRecordIndex())
									.setCreateDcrNumber(new CustomInteger(toString(rec.getInt())));

						}

					}

					if (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixMultipleCode(),
							null)
							&& !Objects.equals(
									nvl(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCenterFixIdent(), "NOPT"),
									"NOPT")
							&& !Objects.equals(
									plTldStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(), "RF")) {
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setCenterFixMultipleCode("0");

					}

					validateLeg("Y");
					controlBlock.setValidated("N");

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWhenValidateRecord executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWhenValidateRecord Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegKeyUp(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegKeyUp Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(), null)
					&& Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), null)
					&& Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getCreateDcrNumber(), null)) {
				clearRecord("");
				if (Objects.equals(system.getLastRecord(), false)) {
					previousRecord("");

				}

			}

			else {
				previousRecord("");

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegKeyUp executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegKeyUp Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegKeyDown(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegKeyDown Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), null)
					&& Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getPathAndTermination(),
							null)) {
				// null;

			}

			else {
				controlBlock.setPreSeq(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum());
				controlBlock
						.setTransitionIdent(plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent());
				controlBlock.setRouteType(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType());
				controlBlock.setAircraftType(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType());
				system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
				plTldStarLeg.getData().add(system.getCursorRecordIndex(), new PlTldStarLeg());
				// nextRecord( "");
				if (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setRouteType(controlBlock.getRouteType());
					plTldStarLeg.getRow(system.getCursorRecordIndex())
							.setTransitionIdent(controlBlock.getTransitionIdent());
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setAircraftType(controlBlock.getAircraftType());
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(controlBlock.getPreSeq() + 10);

				}

				// goItem("pl_tld_star_leg.fix_ident");

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegKeyDown executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegKeyDown Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegKeyCrerec(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegKeyCrerec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapperV2(reqDto, this, true);
			parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this, "system.cursor_block")),
					global.getDataSupplier(), toInteger(global.getProcessingCycle()), toInteger(global.getDcrNumber()),
					parameter.getRecordType(), "CRE"));
			if (Objects.equals(parameter.getUpdRec(), "Y")) {
				if (Objects.equals(system.getCursorBlock(), "PL_TLD_STAR_LEG")
						&& Objects.equals(system.getCursorRecordIndex(), 0)
						&& Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
					system.setCursorRecordIndex(0);

					// goBlock("pl_tld_STAR_segment", "");

					// TODO first_record;
					if (!Objects.equals(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(), null)) {
						// goBlock("pl_tld_STAR_leg", "");
						plTldStarLeg.getRow(system.getCursorRecordIndex())
								.setRouteType(plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType());
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setTransitionIdent(
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setAircraftType(
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getAircraftType());
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(10);

					}

					else {

						goBlock("pl_tld_STAR_segment", "");

						message("please create the segment first.");
						throw new FormTriggerFailureException();

					}

				}

				else {
					controlBlock.setPreSeq(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum());
					controlBlock.setTransitionIdent(
							plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent());
					controlBlock.setRouteType(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType());
					controlBlock.setAircraftType(plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType());
					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);
					plTldStarLeg.getData().add(system.getCursorRecordIndex(), new PlTldStarLeg());
					// createRecord("");
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setRouteType(controlBlock.getRouteType());
					plTldStarLeg.getRow(system.getCursorRecordIndex())
							.setTransitionIdent(controlBlock.getTransitionIdent());
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setAircraftType(controlBlock.getAircraftType());
					plTldStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(-1);

					system.setCursorRecordIndex(system.getCursorRecordIndex() + 1);

					if (!Objects.equals(controlBlock.getRouteType(),
							plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteType())
							|| !Objects.equals(controlBlock.getTransitionIdent(),
									plTldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())
							|| !Objects.equals(controlBlock.getAircraftType(),
									plTldStarLeg.getRow(system.getCursorRecordIndex()).getAircraftType())
							|| Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(),
									null)) {
						system.setCursorRecordIndex(system.getCursorRecordIndex() - 1);

						plTldStarLeg.getRow(system.getCursorRecordIndex())
								.setSequenceNum(controlBlock.getPreSeq() + 10);

					}

					else {
						controlBlock.setNextSeq(plTldStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum());

						system.setCursorRecordIndex(system.getCursorRecordIndex() - 1);

						// TODO
						plTldStarLeg.getRow(system.getCursorRecordIndex()).setSequenceNum(
								Math.floorDiv((controlBlock.getPreSeq() + controlBlock.getNextSeq()), 2));

					}

				}

				plTldStarLeg.getRow(system.getCursorRecordIndex()).setRecordStatus("INSERT");
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setAirportIdent(plTldStar.getAirportIdent());
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setStarIdent(plTldStar.getStarIdent());
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setProcessingCycle(plTldStar.getProcessingCycle());
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setCustomerIdent(plTldStar.getCustomerIdent());
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setAirportIcao(plTldStar.getAirportIcao());
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setDataSupplier(plTldStar.getDataSupplier());
				system.setFormStatus("CHANGED");
				system.setBlockStatus("CHANGED");
				system.setRecordStatus("INSERT");

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegKeyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegKeyCrerec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixIdentOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixIdentOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//// vMessage = refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_LEG", "C");
			// refreshMasterLibrary.checkReferenceInfo("PL_TLD_STAR_LEG", "C");
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_TLD_STAR_LEG','C')
			// parameter.setUpdRec(coreptLib.setActionRestr(toString(nameIn(this,
			// "system.cursor_block")),
			// global.getDataSupplier(), toInteger(global.getProcessingCycle()),
			// toInteger(global.getDcrNumber()), parameter.getRecordType(), "UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// // coreptLib. dspActionMsg
			// //
			// ("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixIdentOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixIdentOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// plTldStarLegFixIdentWhenValidateItem(
	// AirportStarTriggerRequestDto reqDto) throws Exception {
	// log.info(" plTldStarLegFixIdentWhenValidateItem Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try {
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// Object lsIcao = null;
	// Object lsSec = null;
	// Object lsSubSec = null;
	// Boolean lbDummy = null;
	//
	// try {
	// if (Objects.equals(parameter.getFixLov(), "N")) {
	//
	// // TODO
	// //
	// fix_value_prc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent(),plTldStar.getAirportIdent(),plTldStar.getCustomerIdent(),plTldStar.getAirportIcao(),Name_IN("global.data_supplier"),plTldStar.getProcessingCycle(),plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode(),plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode(),plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode());
	//
	// }
	//
	// parameter.setFixLov("N");
	// }
	// // NO_DATA_FOUND
	// catch (NoDataFoundException e) {
	// plTldStarLeg.getRow(system.getCursorRecordIndex()).setFixIcaoCode("");
	//
	// }
	// // TOO_MANY_ROWS
	// catch (TooManyRowsException e) {
	// parameter.setFixIcao(plTldStar.getAirportIcao());
	// // TODO lbDummy = showLov("FIX_LOV");
	//
	// }
	// // OTHERS
	// catch (Exception e) {
	// // null;
	//
	// }
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" plTldStarLegFixIdentWhenValidateItem executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// resDto));
	// } catch (Exception e) {
	// log.error("Error while Executing the plTldStarLegFixIdentWhenValidateItem
	// Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto);
	// }
	// }

	@Override

	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixIdentWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixIdentWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapperV2(reqDto, this, true);
			// Object lsIcao = null;
			// Object lsSec = null;
			// Object lsSubSec = null;
			// Boolean lbDummy = null;

			try {
				if (Objects.equals(parameter.getFixLov(), "N")) {

					fixValuePrcRes res = coreptLib.fixvalueprc(
							plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent(),
							plTldStar.getAirportIdent(),
							plTldStar.getCustomerIdent(),
							plTldStar.getAirportIcao(),
							toString(nameIn(this, "global.data_supplier")), toInteger(plTldStar.getProcessingCycle()),
							plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode(),
							plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode(),
							plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode());
					plTldStarLeg.getRow(system.getCursorRecordIndex())
							.setFixIcaoCode(res.poIcao() == null
									? plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIcaoCode()
									: res.poIcao());
					plTldStarLeg.getRow(system.getCursorRecordIndex())
							.setFixSectionCode(res.poSec() == null
									? plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSectionCode()
									: res.poSec());
					plTldStarLeg.getRow(system.getCursorRecordIndex())
							.setFixSubsectionCode(res.poSubSec() == null
									? plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixSubsectionCode()
									: res.poSubSec());

				}

				parameter.setFixLov("N");
			} catch (NoDataFoundException e) {
				plTldStarLeg.getRow(system.getCursorRecordIndex()).setFixIcaoCode("");

			} catch (TooManyRowsException e) {
				parameter.setFixIcao(plTldStar.getAirportIcao());
				showLov("fixLov");
			} catch (Exception e) {
				;

			}
			// mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixIdentWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixIdentWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixIdentWhenMouseDoubleclick(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixIdentWhenMouseDoubleclick Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Boolean lovRet = null;
			// Integer lnRecCnt = 0;

			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent(), null)) {
					parameter.setFixIcao(null);
					// TODO lovRet = showLov("FIX_LOV");
					if (Objects.equals(lovRet, true)) {
						parameter.setFixLov("Y");

					}

					else {
						parameter.setFixLov("N");

					}

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixIdentWhenMouseDoubleclick executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixIdentWhenMouseDoubleclick Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixIdentKeyListval(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixIdentKeyListval Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Boolean lovRet = null;
			// Integer lnRecCnt = 0;

			if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (!Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getFixIdent(), null)) {
					parameter.setFixIcao(null);
					// TODO lovRet = showLov("FIX_LOV");
					if (Objects.equals(lovRet, true)) {
						parameter.setFixLov("Y");

					}

					else {
						parameter.setFixLov("N");

					}

				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixIdentKeyListval executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixIdentKeyListval Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixIcaoCodeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixIcaoCodeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_TLD_STAR_LEG','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixIcaoCodeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixIcaoCodeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixSectionCodeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixSectionCodeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_TLD_STAR_LEG','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixSectionCodeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixSectionCodeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegFixSubsectionCodeOnError(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegFixSubsectionCodeOnError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// coverity-fixes
			// if (Objects.equals(global.getErrorCode(), 40200)) {
			//// String vMessage = null;
			//
			// // TODO v_message :=
			// // refresh_master_library.check_reference_info('PL_TLD_STAR_LEG','C')
			// // TODO parameter.setUpdRec(setActionRestr(nameIn(this,
			// //
			// "system.cursor_block"),global.getProcessingCycle(),global.getDcrNumber(),global.getDataSupplier(),parameter.getRecordType(),"UPD"));
			// if (Objects.equals(parameter.getUpdRec(), "N")) {
			//
			// // TODO
			// //
			// dsp_action_msg("U",parameter.getRecordType(),nameIn(this,"global.dcr_number"),NVL(nameIn(this,system.getCursorBlock()||".processing_cycle"),global.getProcessingCycle()),nameIn(this,system.getCursorBlock()||".CUSTOMER_IDENT"));
			//
			// }
			//
			// }

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegFixSubsectionCodeOnError executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegFixSubsectionCodeOnError Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWc1WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWc1WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWc1WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWc1WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWc2WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWc2WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWc2WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWc2WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWc3WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWc3WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWc3WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWc3WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegWc4WhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegWc4WhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// TODO Set_waypoint_code --- Program Unit Calling
			setWaypointCode();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegWc4WhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegWc4WhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegMagneticCourseWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegMagneticCourseWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			commonMagneticCourse();

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegMagneticCourseWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegMagneticCourseWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	private void commonMagneticCourse() throws Exception {
		try {

			plTldStarLeg.getRow(system.getCursorRecordIndex()).setMagneticCourse(
					coreptLib.magcourseprc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse()));

			// TODO
			// Mag_Course_Prc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse());
			if (length(plTldStarLeg.getRow(system.getCursorRecordIndex()).getMagneticCourse()) > 4) {

				// TODO dsp_msg("Magnetic Course should not exceed 4 characters");
				coreptLib.dspMsg("Magnetic Course should not exceed 4 characters");
				throw new FormTriggerFailureException();

			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegRouteDistanceWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegRouteDistanceWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			plTldStarLeg.getRow(system.getCursorRecordIndex()).setRouteDistance(
					coreptLib.routedistanceprc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance()));

			// Route_Distance_Prc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance());
			if (length(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRouteDistance()) > 4) {

				// TODO dsp_msg("Route Distance should not exceed 4 characters");
				coreptLib.dspMsg("Route Distance should not exceed 4 characters");
				throw new FormTriggerFailureException();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegRouteDistanceWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegRouteDistanceWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegRnpWhenValidateItem(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegRnpWhenValidateItem Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRnp(), "0")) {
				plTldStarLeg.getRow(system.getCursorRecordIndex())
						.setRnp(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRnp());
			} else {
				plTldStarLeg.getRow(system.getCursorRecordIndex())
						.setRnp(coreptLib.rnpprc(plTldStarLeg.getRow(system.getCursorRecordIndex()).getRnp()));
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegRnpWhenValidateItem executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegRnpWhenValidateItem Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegCloseDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("pl_tld_cntd");
			goItem("pl_tld_star_leg.se9uence_num");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegOpenDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" plTldStarLegOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("pl_tld_cntd");
			goItem("pl_tld_star_leg.atc_ind");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" plTldStarLegOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the plTldStarLegOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void stdStarPreQuery(AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" stdStarPreQuery Executing");

		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			stdStar.getRow(0).setDataSupplier(global.getDataSupplier());
			log.info(" stdStarPreQuery executed successfully");
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> stdStarOnPopulateDetails(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" stdStarOnPopulateDetails Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// String recstat = system.getRecordStatus();
			String startitm = system.getCursorItem();
			Object relId = null;

			// coverity-fixes
			// if ((Objects.equals(recstat, "NEW") || Objects.equals(recstat, "INSERT"))) {
			//
			// // return
			// }

			if (((!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getAirportIdent(), null))
					|| (!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getAirportIcao(), null))
					|| (!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getStarIdent(), null))
					|| (!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getDataSupplier(), null)))) {
				// TODO relId = findRelation("STD_STAR.STD_STAR_STD_STAR_SEGMENT");

				// TODO Query_Master_Details(rel_id,'STD_STAR_SEGMENT') --- Program Unit Calling
				queryMasterDetails(relId, "STD_STAR_SEGMENT");

			}

			if (((!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getAirportIdent(), null))
					|| (!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getAirportIcao(), null))
					|| (!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getStarIdent(), null))
					|| (!Objects.equals(stdStar.getRow(system.getCursorRecordIndex()).getDataSupplier(), null)))) {
				// TODO relId = findRelation("STD_STAR.STD_STAR_STD_STAR_LEG");

				// TODO Query_Master_Details(rel_id,'STD_STAR_LEG') --- Program Unit Calling
				queryMasterDetails(relId, "STD_STAR_LEG");

			}

			if ((!Objects.equals(system.getCursorItem(), startitm))) {
				// goItem(startitm);

				// TODO Check_Package_Failure --- Program Unit Calling
				checkPackageFailure();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdStarOnPopulateDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdStarOnPopulateDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> stdStarSegmentTransitionIdentWhenMouseDoubleclick(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" stdStarSegmentTransitionIdentWhenMouseDoubleclick Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("std_star_leg.transition_ident");

			// TODO first_record;
			while (true) {
				if (Objects.equals(stdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						stdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {

					// TODO next_item;
					break;

				}

				else {
					nextRecord("");

				}
				break;

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdStarSegmentTransitionIdentWhenMouseDoubleclick executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdStarSegmentTransitionIdentWhenMouseDoubleclick Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> stdStarSegmentQualifier2WhenMouseDoubleclick(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" stdStarSegmentQualifier2WhenMouseDoubleclick Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("std_star_leg.transition_ident");

			// TODO first_record;
			while (true) {
				if (Objects.equals(stdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						stdStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {

					// TODO next_item;
					break;

				}

				else {
					nextRecord("");

				}
				break;

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdStarSegmentQualifier2WhenMouseDoubleclick executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdStarSegmentQualifier2WhenMouseDoubleclick Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void stdStarLegPostQuery() throws Exception {
		log.info(" stdStarLegPostQuery Executing");
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			for (StdStarLeg stdStarLeg : stdStarLeg.getData()) {
				if (!Objects.equals(stdStarLeg.getWaypointDescCode(), null)) {
					stdStarLeg.setWc1(substr(rpad(stdStarLeg.getWaypointDescCode(), 4), 1, 1).trim());
					stdStarLeg.setWc2(substr(rpad(stdStarLeg.getWaypointDescCode(), 4), 2, 1).trim());
					stdStarLeg.setWc3(substr(rpad(stdStarLeg.getWaypointDescCode(), 4), 3, 1).trim());
					stdStarLeg.setWc4(substr(rpad(stdStarLeg.getWaypointDescCode(), 4), 4, 1).trim());
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdStarLegPostQuery executed successfully");

		} catch (Exception e) {
			log.error("Error while Executing the stdStarLegPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> stdStarLegCloseDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" stdStarLegCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("std_cntd");

			// ("std_star_leg.se9uence_num");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdStarLegCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdStarLegCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> stdStarLegOpenDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" stdStarLegOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("std_cntd");
			goItem("std_star_leg.atc_ind");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" stdStarLegOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the stdStarLegOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void tldStarPreQuery(AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStarPreQuery Executing");

		try {
			tldStar.getRow(0).setDataSupplier(global.getDataSupplier());
			log.info(" tldStarPreQuery executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the tldStarPreQuery Service");
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> tldStarOnPopulateDetails(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStarOnPopulateDetails Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			// String recstat = system.getRecordStatus();
			String startitm = system.getCursorItem();
			Object relId = null;

			// coverity-fixes
			// if ((Objects.equals(recstat, "NEW") || Objects.equals(recstat, "INSERT"))) {
			//
			// // return
			// }

			if (((!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getAirportIdent(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getAirportIcao(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getStarIdent(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getDataSupplier(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getCustomerIdent(), null)))) {
				// TODO relId = findRelation("TLD_STAR.TLD_STAR_TLD_STAR_SEGMENT");

				// TODO Query_Master_Details(rel_id,'TLD_STAR_SEGMENT') --- Program Unit Calling
				queryMasterDetails(relId, "TLD_STAR_SEGMENT");

			}

			if (((!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getAirportIdent(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getAirportIcao(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getStarIdent(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getDataSupplier(), null))
					|| (!Objects.equals(tldStar.getRow(system.getCursorRecordIndex()).getCustomerIdent(), null)))) {
				// TODO relId = findRelation("TLD_STAR.TLD_STAR_TLD_STAR_LEG");

				// TODO Query_Master_Details(rel_id,'TLD_STAR_LEG') --- Program Unit Calling
				queryMasterDetails(relId, "TLD_STAR_LEG");

			}

			if ((!Objects.equals(system.getCursorItem(), startitm))) {
				goItem(startitm);

				// TODO Check_Package_Failure --- Program Unit Calling
				checkPackageFailure();

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarOnPopulateDetails executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStarOnPopulateDetails Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> tldStarSegmentTransitionIdentWhenMouseDoubleclick(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStarSegmentTransitionIdentWhenMouseDoubleclick Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			goItem("tld_star_leg.transition_ident");

			// TODO first_record;
			while (true) {
				if (Objects.equals(tldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent(),
						tldStarLeg.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {

					// TODO next_item;
					break;

				}

				else {
					nextRecord("");

				}
				break;

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarSegmentTransitionIdentWhenMouseDoubleclick executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStarSegmentTransitionIdentWhenMouseDoubleclick Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public void tldStarLegPostQuery() throws Exception {
		log.info(" tldStarLegPostQuery Executing");
		// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
		// BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			for (TldStarLeg tldStarLeg : tldStarLeg.getData()) {
				if (!Objects.equals(tldStarLeg.getWaypointDescCode(), null)) {
					tldStarLeg.setWc1(substr(rpad(tldStarLeg.getWaypointDescCode(), 4), 1, 1).trim());
					tldStarLeg.setWc2(substr(rpad(tldStarLeg.getWaypointDescCode(), 4), 2, 1).trim());
					tldStarLeg.setWc3(substr(rpad(tldStarLeg.getWaypointDescCode(), 4), 3, 1).trim());
					tldStarLeg.setWc4(substr(rpad(tldStarLeg.getWaypointDescCode(), 4), 4, 1).trim());
				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarLegPostQuery executed successfully");
		} catch (Exception e) {
			log.error("Error while Executing the tldStarLegPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> tldStarLegCloseDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStarLegCloseDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			hideView("tld_cntd");
			goItem("tld_star_leg.se9uence_num");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarLegCloseDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStarLegCloseDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> tldStarLegOpenDetailsButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" tldStarLegOpenDetailsButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			showView("tld_cntd");
			goItem("tld_star_leg.atc_ind");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarLegOpenDetailsButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStarLegOpenDetailsButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> controlBlockPlotWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" controlBlockPlotWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String lsAirportIdent = null;
			String lsAirportIcao = null;
			String lsDataSupplier = null;
			Integer lnProcessingCycle = null;
			String lsCustomerIdent = null;
			String lsPlottingFunctionCommand = null;
			String lsPlottingFunctionDir = null;
			String lsFormName = null;
			String lsTableName = null;
			String lsQuery = null;
			String lsCallPlottingFunction = null;
			String lsCallPlottingFunction1 = null;
			Integer lnRecordCount = null;
			String lsFormId = null;
			String lsPlotUrl = null;
//			   MYPROCESSID  WEBUTILHOST.PROCESSID;
			global.setLsPlotUrl(webPlotUrl);

			// TODO lsFormId = findForm(lower("AIRPORT_STAR"));

			try {
				// lsPlottingFunctionDir = plotGlobalPkg.getinstallationdir();
				// TODO plotGlobalPkg.lnPlotProcessCounter =
				// nvl(plotGlobalPkg.ltab_Process_ID.COUNT
				// TODO PLOT_GLOBAL_PKG.ltab_Process_ID.COUNT, 0) + 1;
				// plotGlobalPkg.lnPlotProcessCounter = nvl(plotGlobalPkg.ltabProcessId);
				throw new FormTriggerFailureException();
			}
			// OTHERS
			catch (Exception e) {
				lsPlottingFunctionDir = "C:\\Program Files\\Honeywell Inc., Aviation Services\\CorePTDU Tool\\";
				lnRecordCount = 1;
			}

			if (Objects.equals(parameter.getRecordType(), "S")
					&& Objects.equals(parameter.getLibraryAccess(), "PRE-LIBRARY")) {
				lsTableName = "PL_STD_STAR";
				lnProcessingCycle = toInteger(plStdStar.getProcessingCycle());
			} else if (Objects.equals(parameter.getRecordType(), "T")
					&& Objects.equals(parameter.getLibraryAccess(), "PRE-LIBRARY")) {
				lsTableName = "PL_TLD_STAR";
				lnProcessingCycle = toInteger(plTldStar.getProcessingCycle());
				lsCustomerIdent = plTldStar.getCustomerIdent();
			} else if (Objects.equals(parameter.getRecordType(), "S")
					&& Objects.equals(parameter.getLibraryAccess(), "MASTER")) {
				lsTableName = "STD_STAR";
			} else if (Objects.equals(parameter.getRecordType(), "T")
					&& Objects.equals(parameter.getLibraryAccess(), "MASTER")) {
				lsTableName = "TLD_STAR";
				lsCustomerIdent = tldStar.getRow(system.getCursorRecordIndex()).getCustomerIdent();
			}
			
			lsFormName  = lsTableName;
			lsAirportIdent = toString(nameIn(this, lsTableName + ".AIRPORT_IDENT"));
			lsAirportIcao = toString(nameIn(this, lsTableName + ".AIRPORT_ICAO"));
			lsDataSupplier = toString(nameIn(this, lsTableName + ".DATA_SUPPLIER"));

			lsQuery = lsDataSupplier + "," + lsAirportIcao + "," + lsAirportIdent + "," + lnProcessingCycle + ","
					+ lsCustomerIdent;
			if (lnRecordCount >= 10) {
				coreptLib.dspMsg(
						"Attn !!!! Maximum of 10 Plot Windows are already opened, please close atleast one Plot Window to open new Plot Window");
			} else {
				if (!Objects.equals(lsAirportIdent, null) && !Objects.equals(lsAirportIcao, null)
						&& !Objects.equals(lsDataSupplier, null)) {
					lsTableName = lsTableName + "_LEG";
					if (!Objects.equals(lsQuery, null)) {
						// TODO
						// Fetch_Plotting_Data.Get_Record_Count(ls_Query,ls_Table_Name,ln_Record_Count);
						Map<String, Object> result = app.executeProcedure("CPTPM", "Get_Record_Count",
								"Fetch_Plotting_Data", null,
								new ProcedureInParameter("pi_squery", lsQuery, OracleTypes.VARCHAR),
								new ProcedureInParameter("pi_sTable_Name", lsTableName, OracleTypes.VARCHAR),
								new ProcedureOutParameter("po_nRec_Count", OracleTypes.NUMBER));
						lnRecordCount = toInteger(result.get("po_nRec_Count"));
					} else {
						lnRecordCount = 0;
					}
					if (lnRecordCount > 0) {
						lsQuery = lsQuery.replace("null", "");
						lsPlotUrl = global.getLsPlotUrl() + "?username=" + global.getUserName() + "&password="
								+ global.getPassword() + "&connectstr=" + global.getConnectString() + "&datatype="
								+ lsTableName + "&query=" + lsQuery;
						if (Objects.equals(displayItemBlock.getGearth(), "Y")) {
							// TODO WEB.SHOW_DOCUMENT(ls_Plot_URL,"_blank");
							createPlot(lsPlotUrl);
						} else {
							lsPlottingFunctionCommand = lsPlottingFunctionDir + "CorePTDU.exe" + " -user:"
									+ global.getUserName() + " -password:" + global.getPassword() + " -connectstr:"
									+ global.getConnectString() + " -datatype:COMPANY_ROUTE" + " -query:" + lsQuery;
							// TODO MY_PROCESS_ID := WEBUTIL_HOST.NonBlocking(ls_Plotting_Function_Command)
							try {
								// TODO
								// plotGlobalPkg.ltabProcessId(plotGlobalPkg.lnPlotProcessCounter).lnProcessId =
								// toNumber(myProcessId.handle);
								// plotGlobalPkg.ltabProcessId(plotGlobalPkg.lnPlotProcessCounter).lsDatatype =
								// TO_CHAR(ls_Form_Id.ID);
							} catch (Exception e) {
								return null;
							}
						}
						goBlock(lsFormName, "");

					} else {
						coreptLib.dspMsg("Invalid/No Data to plot");
					}

				} else {
					coreptLib.dspMsg("Invalid/No Data to plot");
				}

			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlBlockPlotWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlBlockPlotWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// controlBlockStdOverideWhenButtonPressed(AirportStarTriggerRequestDto reqDto)
	// throws Exception{
	// log.info(" controlBlockStdOverideWhenButtonPressed Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// showView("std_over");
	// goItem("control_block.std_override_errors");
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" controlBlockStdOverideWhenButtonPressed executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the controlBlockStdOverideWhenButtonPressed
	// Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// controlBlockTldOverideWhenButtonPressed(AirportStarTriggerRequestDto reqDto)
	// throws Exception{
	// log.info(" controlBlockTldOverideWhenButtonPressed Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// showView("tld_over");
	// goItem("control_block.tld_override_errors");
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" controlBlockTldOverideWhenButtonPressed executed successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the controlBlockTldOverideWhenButtonPressed
	// Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// controlBlockCloseStdOverrideWhenButtonPressed(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" controlBlockCloseStdOverrideWhenButtonPressed Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// hideView("STD_OVER");
	// goItem("pl_std_star.airport_ident");
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" controlBlockCloseStdOverrideWhenButtonPressed executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// controlBlockCloseStdOverrideWhenButtonPressed Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// controlBlockCloseTldOverrideWhenButtonPressed(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" controlBlockCloseTldOverrideWhenButtonPressed Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// hideView("TLD_OVER");
	// goItem("pl_tld_star.airport_ident");
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" controlBlockCloseTldOverrideWhenButtonPressed executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// controlBlockCloseTldOverrideWhenButtonPressed Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// displayItemBlockFormPartNumberWhenNewItemInstance(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" displayItemBlockFormPartNumberWhenNewItemInstance Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// displayItemBlockRefreshButtonWhenButtonPressed(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" displayItemBlockRefreshButtonWhenButtonPressed Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	// OracleHelpers.ResponseMapper(this, resDto);
	// log.info(" displayItemBlockRefreshButtonWhenButtonPressed executed
	// successfully");
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
	// }
	// catch(Exception e) {
	// log.error("Error while Executing the
	// displayItemBlockRefreshButtonWhenButtonPressed Service");
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto); }
	// }
	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilDummyWhenButtonPressed(AirportStarTriggerRequestDto reqDto) throws
	// Exception{
	// log.info(" webutilDummyWhenButtonPressed Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilClientinfoFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilClientinfoFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilFileFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilFileFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilHostFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilHostFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilSessionFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilSessionFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilFiletransferFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilFiletransferFunctionsWhenCustomItemEvent
	// Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilOleFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilOleFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilCApiFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilCApiFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// webutilWebutilBrowserFunctionsWhenCustomItemEvent(AirportStarTriggerRequestDto
	// reqDto) throws Exception{
	// log.info(" webutilWebutilBrowserFunctionsWhenCustomItemEvent Executing");
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
	public void updateAppInstance() {

		// super.displayItemBlock = this.displayItemBlock;
		OracleHelpers.bulkClassMapper(this, displayAlert);
		OracleHelpers.bulkClassMapper(this, coreptLib);
		coreptLib.initialization(this);
		OracleHelpers.bulkClassMapper(this, coreptTemplateTriggerServiceImpl);
		OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		coreptMenuMmbServiceImpl.initialization(this);
		coreptTemplateTriggerServiceImpl.initialization(this);
		OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		refreshMasterLibrary.initialization(this);
		super.app = this.app;
		super.baseInstance = this;
		super.groups = this.groups;
		super.genericNativeQueryHelper = this.genericNativeQueryHelper;
		super.event = this.event;
		super.parameter = this.parameter;
		super.displayAlert = this.displayAlert;
		super.system = this.system;
		super.global = this.global;
		super.blocksOrder = this.blocksOrder;
		super.windows = this.windows;

	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> displayItemBlockRefreshButtonWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" displayItemBlockRefreshButtonWhenButtonPressed Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			Integer totalRows = 0;

			if (Objects.equals(system.getFormStatus(), "CHANGED")) {
				coreptLib.dspMsg("There is changes in the form, please do commit first.");
			} else {
				Integer vButton = 0;

				totalRows = getGroupRowCount(findGroup("refreshRecordsGroup"));
				alertDetails.getCurrent();
				if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {

					vButton = displayAlert.moreButtons(
							"S", "Refresh Record", "You have modified or inserted or deleted " + totalRows
									+ " record(s).\n" + "Do you want to refresh the Master Library now?",
							"Refresh", "Cancel", "");
					OracleHelpers.ResponseMapper(displayAlert, this);
					alertDetails.createNewRecord("displayItemBlockRefreshButtonWhenButtonPressed");
					log.debug("" + vButton);

					throw new AlertException(event, alertDetails);
				} else {
					vButton = alertDetails.getAlertValue("displayItemBlockRefreshButtonWhenButtonPressed",
							alertDetails.getCurrentAlert());
				}
				// coverity-fixes
				log.info("" + vButton);

				if (Objects.equals(vButton, 1)) {
					refreshMasterLibrary.refreshRecords(totalRows);
				}

			}
			OracleHelpers.ResponseMapper(this, resDto);
			log.info("displayItemBlockRefreshButtonWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> onMessage(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyDown(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// keyExit(AirportStarTriggerRequestDto reqDto)
	// throws Exception {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> controlBlockStdOverideWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> controlBlockTldOverideWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> controlBlockCloseStdOverrideWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> controlBlockCloseTldOverrideWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> displayItemBlockFormPartNumberWhenNewItemInstance(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilDummyWhenButtonPressed(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilClientinfoFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilFileFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilHostFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilSessionFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilFiletransferFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilOleFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilCApiFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> webutilWebutilBrowserFunctionsWhenCustomItemEvent(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkPackageFailure() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryMasterDetails(Object relId, String detail) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearAllMasterDetails() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void validateKeys(String pIgnoreRef) throws Exception {

		log.info("validateKeys Executing");
		String query = "";
		Record rec = null;
		try {
			CrAirportProcedure vRecord = new CrAirportProcedure();
			Integer vDcr = null;
			Integer vErr = 0;
			String vAllErrs = null;
			Integer vCycle = 0;
			Integer vProcessingCycle = 0;

			if (Objects.equals(parameter.getRecordType(), "S")) {
				if (Objects.equals(rtrim(plStdStar.getProcessingCycle().toString()), null)) {
					plStdStar.setProcessingCycle(global.getProcessingCycle());

				}

				vProcessingCycle = toInteger(plStdStar.getProcessingCycle());
				if (OracleHelpers.integerGreaterThanEquals(toInteger(global.getRecentCycle()), vProcessingCycle)) {
					vCycle = vProcessingCycle;

				}

				else {
					vCycle = Integer.parseInt(global.getRecentCycle());

				}
				query = "select util1.v_area_code(?) from dual";
				rec = app.selectInto(query, plStdStar.getAreaCode());
				vErr = rec.getInt();

				vDcr = app.executeFunction(Integer.class, "CPT", "check_reference", "VREL2", OracleTypes.INTEGER,
						new ProcedureInParameter("p_view_only", pIgnoreRef, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_table", "pl_std_star", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_dcr", toInteger(plStdStar.getCreateDcrNumber()),
								OracleTypes.NUMBER),
						new ProcedureInParameter("p_record_type", "S", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_section", "P", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_subsection", "A", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_proc_cycle", vCycle, OracleTypes.NUMBER),
						new ProcedureInParameter("p_icao", plStdStar.getAirportIcao(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_ident", plStdStar.getAirportIdent(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_cust", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_2nd_icao", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_2nd_ident", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_in_house", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_record_cycle", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_run_loc", null, OracleTypes.VARCHAR));

				if (Objects.equals(pIgnoreRef, "N")) {

					setUpdateDcr("PL_STD_STAR", Optional.ofNullable(vRecord));

				}

			}

			else {
				coreptLib.validateextrafields(toInteger(plTldStar.getProcessingCycle()),
						plTldStar.getGeneratedInHouseFlag());
				if (Objects.equals(rtrim(toString(plTldStar.getProcessingCycle())), null)) {
					plTldStar.setProcessingCycle(global.getProcessingCycle());

				}

				vProcessingCycle = toInteger(plTldStar.getProcessingCycle());
				vCycle = vProcessingCycle;
				if (Objects.equals(rtrim(plTldStar.getGeneratedInHouseFlag()), null)) {
					plTldStar.setGeneratedInHouseFlag("Y");

				}
				query = "select util1.v_customer(?) from dual";
				rec = app.selectInto(query, plTldStar.getCustomerIdent());
				vErr = rec.getInt();

				vDcr = app.executeFunction(Integer.class, "CPT", "check_reference", "VREL2", OracleTypes.INTEGER,
						new ProcedureInParameter("p_view_only", pIgnoreRef, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_table", "pl_tld_star", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_dcr", toInteger(plTldStar.getCreateDcrNumber()),
								OracleTypes.NUMBER),
						new ProcedureInParameter("p_record_type", "T", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_section", "P", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_subsection", "A", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_proc_cycle", vCycle, OracleTypes.NUMBER),
						new ProcedureInParameter("p_icao", plTldStar.getAirportIcao(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_ident", plTldStar.getAirportIdent(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_cust", plTldStar.getCustomerIdent(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_2nd_icao", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_2nd_ident", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_in_house", plTldStar.getGeneratedInHouseFlag(),
								OracleTypes.VARCHAR),
						new ProcedureInParameter("p_record_cycle", null, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_run_loc", "DU", OracleTypes.VARCHAR));

				if (Objects.equals(pIgnoreRef, "N")) {

					setUpdateDcr("PL_TLD_STAR", Optional.ofNullable(vRecord));

				}

			}
			if (vErr > 0 || Objects.equals(vDcr, null)) {
				if (vErr > 0) {
					if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErr)) {
						vAllErrs = "* " + getNullClean(toChar(vErr)) + " - " + coreptLib.getErrText(vErr);

					}

				}

				if (Objects.equals(vDcr, null)) {
					vAllErrs = getNullClean(vAllErrs) + " * 216 - " + coreptLib.getErrText(216);

				}
				controlBlock.setKeyErrors(getNullClean(vAllErrs) + chr(10));
				SetMessage("Y");

				if (Objects.equals(parameter.getRecordType(), "S")) {
					controlBlock.setStdValidationErrors(
							vAllErrs + (plStdStar.getRefInfo() == null ? "" : plStdStar.getRefInfo()));
					if (Objects.equals(plStdStar.getRefInfo(), null)) {
						if (Arrays.asList("Y", "S", "H", "W", "N", "O")
								.contains(nvl(plStdStar.getValidateInd(), "Y"))) {
							plStdStar.setValidateInd("I");
							plStdStar.setOldValidateInd("I");
							controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);

						}

					}

					else {
						setItemProperty("control_block.std_validation_errors", VISIBLE, PROPERTY_TRUE);
						setItemProperty("control_block.std_validation_errors", ENABLED, PROPERTY_TRUE);

					}

				}

				else {
					controlBlock.setTldValidationErrors(
							vAllErrs + (plTldStar.getRefInfo() == null ? "" : plTldStar.getRefInfo()));
					if (Objects.equals(plTldStar.getRefInfo(), null)) {
						if (Arrays.asList("Y", "S", "H", "W", "N", "O")
								.contains(nvl(plTldStar.getValidateInd(), "Y"))) {
							plTldStar.setValidateInd("I");
							plTldStar.setOldValidateInd("I");
							controlBlock.setCountInvalid(controlBlock.getCountInvalid() + 1);

						}

					}

					else {
						setItemProperty("control_block.tld_validation_errors", VISIBLE, PROPERTY_TRUE);
						setItemProperty("control_block.tld_validation_errors", ENABLED, PROPERTY_TRUE);

					}

				}
			}

			else {
				controlBlock.setKeyErrors(null);

				SetMessage("N");

			}

			log.info("validateKeys Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing validateKeys" + e.getMessage());
			throw e;

		}
	}

	@Override
	public void updateProcTypeAndAltitude(String pMaster, String pTerminal) throws Exception {
		log.info("updateProcTypeAndAltitude Executing");
		// String query = "";
		// Record rec = null;
		try {
			String vCust = null;
			Integer vMaxAltitude = 0;
			String vType = null;
			String vTerminal = null;

			if (Objects.equals(parameter.getRecordType(), "T")) {
				vCust = toString(OracleHelpers.nameIn(this, pMaster + ".customerIdent"));

			}

			if (Objects.equals(pTerminal, "A")) {
				vTerminal = "airport";

			}

			else {
				vTerminal = "heliport";

			}
			// coverity-fixes
			log.info(vTerminal);

			if (pMaster.equals("PL_STD_STAR") && !Objects.equals(plStdStarSegment.getRow(0).getRouteType(), null)) {
				for (PlStdStarSegment seg : plStdStarSegment.getData()) {
					if (!Objects.equals(seg.getRecordStatus(), "NEW")) {
						vMaxAltitude = app.executeFunction(Integer.class, "CPT", "set_max_trans_altitude",
								"check_procedures", OracleTypes.INTEGER,
								new ProcedureInParameter("p_record_type", parameter.getRecordType(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_proc", "STAR", OracleTypes.VARCHAR),
								new ProcedureInParameter("p_terminal_type", pTerminal, OracleTypes.VARCHAR),
								new ProcedureInParameter("p_terminal_ident", seg.getAirportIdent(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_terminal_icao", seg.getAirportIcao(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_proc_ident", seg.getStarIdent(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_route_type", seg.getRouteType(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_trans_ident", seg.getTransitionIdent(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_cust", vCust, OracleTypes.VARCHAR),
								new ProcedureInParameter("p_dcr", toInteger(seg.getCreateDcrNumber()),
										OracleTypes.NUMBER),
								new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_cycle", seg.getProcessingCycle(), OracleTypes.NUMBER),
								new ProcedureInParameter("p_aircraft_type", seg.getAircraftType(), OracleTypes.VARCHAR)

						);
					}
					if (!Objects.equals(nvl(vMaxAltitude, -1), nvl(seg.getMaxTransAltitude(), -1))) {
						seg.setMaxTransAltitude(vMaxAltitude);
						if (seg.getRecordStatus().equals("QUERIED")) {
							seg.setRecordStatus("CHANGED");
						}
					}

				}

			}
			if (pMaster.equals("PL_TLD_STAR") && !Objects.equals(plTldStarSegment.getRow(0).getRouteType(), null)) {
				for (PlTldStarSegment seg : plTldStarSegment.getData()) {

					if (!Objects.equals(seg.getRecordStatus(), "NEW")) {

						vMaxAltitude = app.executeFunction(Integer.class, "CPT", "set_max_trans_altitude",
								"check_procedures", OracleTypes.INTEGER,
								new ProcedureInParameter("p_record_type", parameter.getRecordType(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_proc", "STAR", OracleTypes.VARCHAR),
								new ProcedureInParameter("p_terminal_type", pTerminal, OracleTypes.VARCHAR),
								new ProcedureInParameter("p_terminal_ident", seg.getAirportIdent(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_terminal_icao", seg.getAirportIcao(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_proc_ident", seg.getStarIdent(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_route_type", seg.getRouteType(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_trans_ident", seg.getTransitionIdent(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_cust", vCust, OracleTypes.VARCHAR),
								new ProcedureInParameter("p_dcr", toInteger(seg.getCreateDcrNumber()),
										OracleTypes.NUMBER),
								new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_cycle", seg.getProcessingCycle(), OracleTypes.NUMBER),
								new ProcedureInParameter("p_aircraft_type", seg.getAircraftType(), OracleTypes.VARCHAR)

						);
					}
					if (!Objects.equals(nvl(vMaxAltitude, -1), nvl(seg.getMaxTransAltitude(), -1))) {

						seg.setMaxTransAltitude(vMaxAltitude);
						if (seg.getRecordStatus().equals("QUERIED")) {
							seg.setRecordStatus("CHANGED");
						}
					}
				}
			}

			if (pMaster.equals("PL_TLD_STAR")) {
				for (PlTldStarSegment seg : plTldStarSegment.getData()) {
					vType = app.executeFunction(String.class, "CPT", "set_procedure_type", "check_procedures",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_record_type", parameter.getRecordType(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_proc", "STAR", OracleTypes.VARCHAR),
							new ProcedureInParameter("p_terminal_type", pTerminal, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_terminal_ident", plTldStar.getAirportIdent(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_terminal_icao", plTldStar.getAirportIcao(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_proc_ident", plTldStar.getStarIdent(), OracleTypes.VARCHAR),

							new ProcedureInParameter("p_cust", vCust, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_dcr", toInteger(seg.getCreateDcrNumber()), OracleTypes.INTEGER),
							new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_cycle", plTldStar.getProcessingCycle(), OracleTypes.INTEGER)

					);
				}
			}
			if (pMaster.equals("PL_STD_STAR")) {
				for (PlStdStarSegment seg : plStdStarSegment.getData()) {
					vType = app.executeFunction(String.class, "CPT", "set_procedure_type", "check_procedures",
							OracleTypes.VARCHAR,
							new ProcedureInParameter("p_record_type", parameter.getRecordType(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_proc", "STAR", OracleTypes.VARCHAR),
							new ProcedureInParameter("p_terminal_type", pTerminal, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_terminal_ident", plStdStar.getAirportIdent(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_terminal_icao", plStdStar.getAirportIcao(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_proc_ident", plStdStar.getStarIdent(), OracleTypes.VARCHAR),

							new ProcedureInParameter("p_cust", vCust, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_dcr", toInteger(seg.getCreateDcrNumber()), OracleTypes.INTEGER),
							new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_cycle", plStdStar.getProcessingCycle(), OracleTypes.INTEGER)

					);
				}
			}
			pMaster = upper(toSnakeCase(pMaster));

			if (!Objects.equals(vType, nvl(nameIn(this, pMaster + "." + substr(pMaster, 8) + "Type"), "A"))) {
				copy(vType, pMaster + "." + substr(pMaster, 8) + "_Type");

				copy("CHANGED", pMaster + "." + "recordStatus");

			}
			system.setFormStatus("CHANGED");
			log.info("updateProcTypeAndAltitude Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing updateProcTypeAndAltitude" + e.getMessage());
			throw e;

		}
	}

	public String vOneProc(String pRecordType, String pProc, String pTerminalType, String pTerminalIdent,
			String pTerminalIcao, String pProcIdent, String pCust, Integer pDcr, String pSupplier, Integer pCycle,
			String pErrorList, String pIgnoreRef) throws Exception {
		log.info("vOneProc Executing");
		String query = null;
		Record rec = null;
		OracleArray<LegRec> vLegs = new OracleArray<>();
		OracleArray<SegRec> vSegs = new OracleArray<>();
		Integer vNsegs = null;
		Integer vNlegs = null;
		Integer vErr = null;
		String vAllErrors = null;
		Boolean vIsMap = false;
		// Integer vMleg = null;
		// v_exception exception;
		String segRouteType = null;
		String segTransition = null;
		Integer vMagCourse1 = null;
		Integer vMagCourse2 = null;
		Integer vMagCourseDiff = null;
		String vTable = null;
		String vInHouse = null;
		Integer vCount = null;
		String vAltitude = "N";
		Integer vRunwayDcr1 = null;
		Integer vRunwayDcr2 = null;
		Integer vDistance = null;
		String vExitRoute = null;
		List<Integer> vErrList = new ArrayList<>();
		Integer vErrNum = 0;
		Integer vSeq = null;
		String vRunwayTransition = "N";
		Integer vProcessingCycle = null;
		SegRec rseg = new SegRec();
		String vS1 = null;
		String vS2 = null;
		String lsProcErr = null;
		// String vStarTransition = ",";
		// String vSidTransition = ",";
		// String vApprTransition = ",";
		// String vAircraftType = null;
		String vTransitionAircraftType = ",";
		String vCommonAircraftType = null;
		String vInvalidAircraftType = "N";
		String vStarEnrtTransition = null;
		// String vSidEnrtTransition = null;
		Integer lnEnrtTransCount = 0;
		String vDupEnrtFound = "N";
		String vEnrtTransitionAircraftTyp = ",";
		String vRwyTransitionAircraftType = ",";
		String vInvalidValues = null;
		String lFirstProcDesignMagVar = null;

		try {

			// TODO fill_segs(p_record_type, v_segs, v_nsegs);
			fillSegsRes fillSegRes = fillSegs(pRecordType, new ArrayList<>(), vNsegs);
			vSegs = new OracleArray<>(fillSegRes.pSegs());
			vSegs.setParamClass(SegRec::new);
			vNsegs = fillSegRes.vNsegs() == null ? toInteger(0) : fillSegRes.vNsegs();

			if (Objects.equals(vNsegs, 0)) {
				lsProcErr = "*" + toChar(5640) + " - " + coreptLib.getErrText(5640);
				throw new vException(lsProcErr);
			}
			if (Objects.equals(pRecordType, "T")) {
				vTable = "PL_TLD_STAR";
				vInHouse = plTldStar.getGeneratedInHouseFlag();
				vProcessingCycle = toInteger(nvl(toChar(plTldStar.getProcessingCycle()), global.getProcessingCycle()));
			} else {
				vTable = "PL_STD_STAR";
				vInHouse = "N";
				vProcessingCycle = toInteger(nvl(toChar(plStdStar.getProcessingCycle()), global.getProcessingCycle()));
			}

			for (int i = 1; i <= vNsegs; i++) {

				rseg.setQualifier1(vSegs.get(i).getQualifier1());
				rseg.setQualifier2(vSegs.get(i).getQualifier2());
				rseg.setAircraftType(vSegs.get(i).getAircraftType());
				rseg.setTransitionIdent(vSegs.get(i).getTransitionIdent());
				rseg.setProcDesignMagVarInd(vSegs.get(i).getProcDesignMagVarInd());

				if (Objects.equals(instr(nvl(vExitRoute, "*"), vSegs.get(i).getRouteType()), 0)) {
					vExitRoute = getNullClean(vExitRoute) + vSegs.get(i).getRouteType();
				} else {

					if (Arrays.asList("2", "5", "8", "M").contains(vSegs.get(i).getRouteType())) {
						if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 731)) {
							vAllErrors = getNullClean(vAllErrors) + "-> * 731 - " + coreptLib.getErrText(731) + chr(10);
						}
					}
				}

				if (Arrays.asList("2", "5", "8", "M").contains(vSegs.get(i).getRouteType())) {

					vCommonAircraftType = vSegs.get(i).getAircraftType();
				} else if (Arrays.asList("1", "4", "7", "F").contains(vSegs.get(i).getRouteType())) {

					if (vStarEnrtTransition != null) {
						lnEnrtTransCount = toInteger(nvl(length(vStarEnrtTransition), 0))
								- toInteger(nvl(length(replace(vStarEnrtTransition, ',', "")), 0)) + 1;

						for (int j = 0; j <= lnEnrtTransCount; j++) {

							// IF REGEXP_SUBSTR (NVL(v_star_enrt_transition,'*'), '[^,]+', 1, i) =
							// rseg.transition_ident THEN
							if (Objects.equals(
									OracleHelpers.getRegexpatter(nvl(vStarEnrtTransition, "*"), "[^,]+", 1, i),
									rseg.getTransitionIdent())) {
								vDupEnrtFound = "Y";
								break;
							} else {
								vDupEnrtFound = "N";
							}

						}

						if (Objects.equals(vDupEnrtFound, "N")) {
							vStarEnrtTransition = vStarEnrtTransition + "," + rseg.getTransitionIdent();
						} else {
							Boolean dbCall = app.executeFunction(Boolean.class, "CPT", "coreptLib. IS_OVERRIDE_ERROR",
									"UTIL1", OracleTypes.BOOLEAN,
									new ProcedureInParameter("p_supplier", global.getDataSupplier(),
											OracleTypes.VARCHAR),
									new ProcedureInParameter("p_cycle", vProcessingCycle, OracleTypes.NUMBER),
									new ProcedureInParameter("p_data_type", "STAR", OracleTypes.VARCHAR),
									new ProcedureInParameter("p_err_id", 5677, OracleTypes.NUMBER));

							if (Objects.equals(dbCall, false)) {

								lsProcErr = getNullClean(lsProcErr) + "* Route Type" + rseg.getRouteType()
										+ " , Transition " + rseg.getTransitionIdent() + "  ->" + toChar(5677) + " - "
										+ coreptLib.getErrText(5677) + chr(10);
							}
						}
					} else {
						vStarEnrtTransition = rseg.getTransitionIdent();
					}

					vEnrtTransitionAircraftTyp = vEnrtTransitionAircraftTyp + rseg.getAircraftType() + ",";
				} else {
					vRwyTransitionAircraftType = vRwyTransitionAircraftType + rseg.getAircraftType() + ",";
				}

				if (Arrays.asList("3", "6", "9", "S").contains(vSegs.get(i).getRouteType())) {
					segRouteType = "369S";
				} else if (Arrays.asList("2", "5", "8", "M").contains(vSegs.get(i).getRouteType())) {
					segTransition = getNullClean(segTransition) + vSegs.get(i).getTransitionIdent() + ",";
				}

				Map<String, Object> dbCall = app.executeProcedure("CPT", "check_sid_star_transition", "chkprc_util",
						new ProcedureInParameter("p_route_type", vSegs.get(i).getRouteType(), OracleTypes.VARCHAR),
						new ProcedureInParameter("p_transition_ident", vSegs.get(i).getTransitionIdent(),
								OracleTypes.VARCHAR),
						new ProcedureInParameter("v_table", vTable, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_record_type", pRecordType, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_proc", pProc, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_terminal_ident", pTerminalIdent, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_terminal_icao", pTerminalIcao, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_cust", pCust, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_dcr", pDcr, OracleTypes.NUMBER),
						new ProcedureInParameter("p_supplier", pSupplier, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_cycle", pCycle, OracleTypes.NUMBER),
						new ProcedureInParameter("p_in_house", vInHouse, OracleTypes.VARCHAR),
						new ProcedureInOutParameter("v_runway_transition", vRunwayTransition, OracleTypes.VARCHAR),
						new ProcedureOutParameter("v_runway_dcr_1", OracleTypes.NUMBER),
						new ProcedureOutParameter("v_err_return", OracleTypes.NUMBER),
						new ProcedureInParameter("p_view_only", pIgnoreRef, OracleTypes.VARCHAR),
						new ProcedureInParameter("p_run_loc", "DU", OracleTypes.VARCHAR));
				vRunwayTransition = toString(dbCall.get("v_runway_transition"));
				BigDecimal bd1 = (BigDecimal) dbCall.get("v_runway_dcr_1");
				vRunwayDcr1 = bd1 != null ? bd1.intValue() : null;
				BigDecimal bd = (BigDecimal) dbCall.get("v_err_return");
				vErrNum = bd != null ? bd.intValue() : null;
				if (vErrNum != null) {

					if (vErrNum > 0) {

						if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErrNum)) {

							vAllErrors = getNullClean(vAllErrors) + "-> * " + toChar(vErrNum) + " - "
									+ coreptLib.getErrText(vErrNum) + chr(10);
						}
					}
				}

				Pair<List<LegRec>, Integer> fillLegsRes = fillLegs(pRecordType, vSegs.get(i).getRouteType(),
						vSegs.get(i).getTransitionIdent(), vSegs.get(i).getAircraftType(), new ArrayList<>(), vNlegs);

				vLegs = new OracleArray<>(fillLegsRes.getFirst());
				vLegs.setParamClass(LegRec::new);
				vNlegs = fillLegsRes.getSecond() != null ? fillLegsRes.getSecond() : 0;
				// TODO fill_legs(p_record_type, v_segs(i).route_type,
				// v_segs(i).transition_ident,v_segs(i).aircraft_type, --65347
				// v_legs, v_nlegs);

				if (vNlegs == 0) {
					lsProcErr = getNullClean(lsProcErr) + "* Route Type '" + vSegs.get(i).getRouteType()
							+ "', Transition '" + vSegs.get(i).getTransitionIdent() + "'Aircraft type'"
							+ vSegs.get(i).getAircraftType() + "' ->" + toChar(1000) + "-" + coreptLib.getErrText(1000);
					throw new vException(lsProcErr);
				}
				if (vNlegs > 1) {
					for (int j = 2; j <= vNlegs; j++) {

						BigDecimal bigDeci = app.executeFunction(BigDecimal.class, "CPT", "V_LEG_SEQUENCE",
								"CHKPRC_UTIL", OracleTypes.NUMBER,
								new ProcedureInParameter("p_leg", vLegs.get(j - 1).getPathAndTermination(),
										OracleTypes.VARCHAR),
								new ProcedureInParameter("p_next_leg", vLegs.get(j).getPathAndTermination(),
										OracleTypes.VARCHAR));
						vErr = bigDeci != null ? bigDeci.intValue() : null;

						// TODO v_err := chkprc_util.v_leg_sequence(v_legs(i-1).path_and_termination,
						// v_legs(i).path_and_termination)
						if (vErr != null && vErr > 0) {
							if (Objects.equals(vLegs.get(j - 1).getPathAndTermination(), "TF")
									&& Objects.equals(vLegs.get(j).getPathAndTermination(), "DF")
									&& Arrays.asList("B", "Y")
											.contains(substr(vLegs.get(j - 1).getWaypointDescCode(), 2, 1))) {

							}

							else {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErr)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#"
											+ vLegs.get(j - 1).getSequenceNum() + " - " + toChar(vErr) + " - "
											+ coreptLib.getErrText(vErr) + ": The leg'"
											+ vLegs.get(j - 1).getPathAndTermination() + " ' " + " is followed by '"
											+ vLegs.get(j).getPathAndTermination() + " '" + chr(10);

								}

							}

						}

						if (Objects.equals(vLegs.get(j - 1).getPathAndTermination(), "PI")
								&& Objects.equals(vLegs.get(j).getPathAndTermination(), "CF")
								&& !Objects.equals(vLegs.get(j).getTurnDirection(), null)) {
							if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 1187)) {
								vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j - 1).getSequenceNum()
										+ " - " + toChar(1187) + " - " + coreptLib.getErrText(1187) + chr(10);

							}

						}

						if (Objects.equals(vLegs.get(j - 1).getPathAndTermination(), "IF")
								&& Arrays.asList("FA", "FC", "FD", "FM", "HA", "HF", "HM", "PI")
										.contains(vLegs.get(j).getPathAndTermination())
								&& Objects.equals(nvl(vLegs.get(j - 1).getAltDescription(), "$"),
										nvl(vLegs.get(j).getAltDescription(), "$"))) {

							query = """
									select  Decode ( (SUBSTR(nvl(RTRIM(LTRIM(?)),'0'),1,2)) , 'FL' ,(SUBSTR(nvl(RTRIM(LTRIM(?)),'0'),3) * 100), TO_NUMBER(nvl(RTRIM(LTRIM(?)),'0')))
																from dual
									""";
							rec = app.selectInto(query, vLegs.get(i - 1).getAlt1(), vLegs.get(i - 1).getAlt1(),
									vLegs.get(i - 1).getAlt1());
							vS1 = rec.getString();

							query = """
									select  Decode ( (SUBSTR(nvl(RTRIM(LTRIM(?)),'0'),1,2)) , 'FL' ,(SUBSTR(nvl(RTRIM(LTRIM(?)),'0'),3) * 100), TO_NUMBER(nvl(RTRIM(LTRIM(?)),'0')))
																from dual
									""";
							rec = app.selectInto(query, vLegs.get(i).getAlt1(), vLegs.get(i).getAlt1(),
									vLegs.get(i).getAlt1());
							vS2 = rec.getString();
							if (Objects.equals(vS1, vS2)) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 1159)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 1159 + " - "
											+ coreptLib.getErrText(1159) + chr(10);

								}

							}

						}

						if (Arrays.asList("CF", "DF", "IF", "TF").contains(vLegs.get(j - 1).getPathAndTermination())
								&& Objects.equals(vLegs.get(j).getPathAndTermination(), "PI")) {
							if (!Objects.equals(vLegs.get(j - 1).getFixIdent() + vLegs.get(j - 1).getFixIcaoCode()
									+ vLegs.get(j - 1).getFixSectionCode() + vLegs.get(j - 1).getFixSubsectionCode(),
									vLegs.get(j).getFixIdent() + vLegs.get(j).getFixIcaoCode()
											+ vLegs.get(j).getFixSectionCode() + vLegs.get(j).getFixSubsectionCode())) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 960)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 960 + " - "
											+ coreptLib.getErrText(960) + chr(10);

								}

							}

						}

						if (Objects.equals(vLegs.get(j - 1).getPathAndTermination(), "AF")
								&& Objects.equals(vLegs.get(j).getPathAndTermination(), "AF")) {
							if (!Objects.equals(
									vLegs.get(j - 1).getRecommNavaidIdent() + vLegs.get(j - 1).getRecommNavaidIcaoCode()
											+ vLegs.get(j - 1).getRecommNavaidSection()
											+ vLegs.get(j - 1).getRecommNavaidSubsection(),
									vLegs.get(j).getRecommNavaidIdent() + vLegs.get(j).getRecommNavaidIcaoCode()
											+ vLegs.get(j).getRecommNavaidSection()
											+ vLegs.get(j).getRecommNavaidSubsection())) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 961)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 961 + " - "
											+ coreptLib.getErrText(961) + chr(10);

								}

							}

							if (OracleHelpers.doublegreaterThan(
									abs(OracleHelpers.sub(vLegs.get(j - 1).getRho(), vLegs.get(j).getRho())), .87)) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 962)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 962 + " - "
											+ coreptLib.getErrText(962) + chr(10);

								}

							}

						}

						if (Objects.equals(vLegs.get(j - 1).getPathAndTermination(), "FD")
								&& Objects.equals(vLegs.get(j).getPathAndTermination(), "AF")) {
							if (!Objects.equals(
									vLegs.get(j - 1).getRecommNavaidIdent() + vLegs.get(j - 1).getRecommNavaidIcaoCode()
											+ vLegs.get(j - 1).getRecommNavaidSection()
											+ vLegs.get(j - 1).getRecommNavaidSubsection(),
									vLegs.get(j).getRecommNavaidIdent() + vLegs.get(j).getRecommNavaidIcaoCode()
											+ vLegs.get(j).getRecommNavaidSection()
											+ vLegs.get(j).getRecommNavaidSubsection())) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 963)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 963 + " - "
											+ coreptLib.getErrText(963) + chr(10);

								}

							}

						}

						if (Arrays.asList("CD", "VD").contains(vLegs.get(j - 1).getPathAndTermination())
								&& Objects.equals(vLegs.get(j).getPathAndTermination(), "AF")) {

							try {
								vDistance = toInteger(vLegs.get(j - 1).getRouteDistance()) / 10;
								if (!Objects.equals(vDistance.doubleValue(), vLegs.get(j).getRho())) {
									if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR",
											964)) {
										vAllErrors = getNullClean(vAllErrors) + "->  Seq#"
												+ vLegs.get(j).getSequenceNum() + ", "
												+ vLegs.get(j - 1).getPathAndTermination() + "/"
												+ vLegs.get(j).getPathAndTermination() + " - " + 964 + " - "
												+ coreptLib.getErrText(964) + chr(10);

									}

								}

							}
							// others
							catch (Exception e) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 964)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 964 + " - "
											+ coreptLib.getErrText(964) + chr(10);

								}

							}
							if (!Objects.equals(
									vLegs.get(j - 1).getRecommNavaidIdent() + vLegs.get(j - 1).getRecommNavaidIcaoCode()
											+ vLegs.get(j - 1).getRecommNavaidSection()
											+ vLegs.get(j - 1).getRecommNavaidSubsection(),
									vLegs.get(j).getRecommNavaidIdent() + vLegs.get(j).getRecommNavaidIcaoCode()
											+ vLegs.get(j).getRecommNavaidSection()
											+ vLegs.get(j).getRecommNavaidSubsection())) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 965)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 965 + " - "
											+ coreptLib.getErrText(965) + chr(10);

								}

							}

						}

						if (Arrays.asList("AF", "CF", "DF", "IF", "RF", "TF", "HA", "HF", "HM")
								.contains(vLegs.get(j - 1).getPathAndTermination())
								&& Arrays.asList("FA", "FC", "FD", "FM")
										.contains(vLegs.get(j).getPathAndTermination())) {
							if (!Objects.equals(vLegs.get(j - 1).getFixIdent(), vLegs.get(j).getFixIdent())) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 966)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 966 + " - "
											+ coreptLib.getErrText(966) + chr(10);

								}

							}

						}

						if (Arrays.asList("CI", "VI").contains(vLegs.get(j - 1).getPathAndTermination())
								&& !Objects.equals(rtrim(vLegs.get(j - 1).getRecommNavaidIdent()), null)) {
							if (!Objects.equals(
									vLegs.get(j - 1).getRecommNavaidIdent() + vLegs.get(j - 1).getRecommNavaidIcaoCode()
											+ vLegs.get(j - 1).getRecommNavaidSection()
											+ vLegs.get(j - 1).getRecommNavaidSubsection(),
									vLegs.get(j).getRecommNavaidIdent() + vLegs.get(j).getRecommNavaidIcaoCode()
											+ vLegs.get(j).getRecommNavaidSection()
											+ vLegs.get(j).getRecommNavaidSubsection())) {
								if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 967)) {
									vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(j).getSequenceNum()
											+ ", " + vLegs.get(j - 1).getPathAndTermination() + "/"
											+ vLegs.get(j).getPathAndTermination() + " - " + 967 + " - "
											+ coreptLib.getErrText(967) + chr(10);

								}

							}

						}

						try {
							if (!Objects.equals(rtrim(vLegs.get(j - 1).getMagneticCourse()), null)
									&& !Objects.equals(rtrim(vLegs.get(j).getMagneticCourse()), null)) {
								if (Objects.equals(substr(vLegs.get(j - 1).getMagneticCourse(), 4, 1), "T")) {
									vMagCourse1 = toInteger(substr(vLegs.get(j - 1).getMagneticCourse(), 1, 3));

								}

								else {
									vMagCourse1 = toInteger(vLegs.get(j - 1).getMagneticCourse()) / 10;

								}
								if (Objects.equals(substr(vLegs.get(j).getMagneticCourse(), 4, 1), "T")) {
									vMagCourse2 = toInteger(substr(vLegs.get(j).getMagneticCourse(), 1, 3));

								}

								else {
									vMagCourse2 = toInteger(vLegs.get(j).getMagneticCourse()) / 10;

								}
								vMagCourseDiff = (int) Math.round(abs(vMagCourse1 - vMagCourse2));
								if (vMagCourseDiff >= 135 && vMagCourseDiff <= 225
										&& !Arrays.asList(135, 225).contains(vMagCourseDiff)) {
									if (!Objects.equals(vLegs.get(j).getPathAndTermination(), "IF")
											&& Arrays.asList("AF", "FM", "HM", "PI", "VM")
													.contains(vLegs.get(j - 1).getPathAndTermination())
											&& !(Objects.equals(vLegs.get(j - 1).getPathAndTermination(), "RF")
													&& Arrays.asList("RF", "AF")
															.contains(vLegs.get(j).getPathAndTermination()))
											&& Objects.equals(rtrim(vLegs.get(j).getTurnDirection()), null)) {
										if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR",
												929)) {
											vAllErrors = getNullClean(vAllErrors) + "->  Seq#"
													+ vLegs.get(j).getSequenceNum() + ", "
													+ vLegs.get(j - 1).getPathAndTermination() + "/"
													+ vLegs.get(j).getPathAndTermination() + " - " + 929 + " - "
													+ coreptLib.getErrText(929) + chr(10);

										}

									}

									if (!Arrays.asList("AF", "DF", "HA", "HF", "HM", "IF", "PI", "FM", "VM")
											.contains(vLegs.get(j - 1).getPathAndTermination())
											&& !Arrays.asList("AF", "DF", "HA", "HF", "HM", "IF", "PI", "RF")
													.contains(vLegs.get(j).getPathAndTermination())
											&& !Objects.equals(rtrim(vLegs.get(j).getTurnDirection()), "E")
											&& Objects.equals(rtrim(vLegs.get(j).getTurnDirValid()), null)) {
										if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR",
												930)) {
											vAllErrors = getNullClean(vAllErrors) + "->  Seq#"
													+ vLegs.get(j).getSequenceNum() + ", "
													+ vLegs.get(j - 1).getPathAndTermination() + "/"
													+ vLegs.get(j).getPathAndTermination() + " - " + 930 + " - "
													+ coreptLib.getErrText(930) + chr(10);

										}

									}

								}

							}

						}
						// others
						catch (Exception e) {
							// TODO null;

						}
						if (!Objects.equals(rtrim(vLegs.get(j - 1).getAlt1()), null)) {
							vAltitude = "Y";

						}

					}

				}

				if (Objects.equals(vLegs.get(vNlegs).getPathAndTermination(), "VM")) {
					if (!Objects.equals(vLegs.get(vNlegs).getFixIdent() + vLegs.get(vNlegs).getFixIcaoCode(),
							pTerminalIdent + pTerminalIcao)
							|| !Objects.equals(
									vLegs.get(vNlegs).getFixSectionCode() + vLegs.get(vNlegs).getFixSubsectionCode(),
									"PA")) {
						if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 975)) {
							vAllErrors = getNullClean(vAllErrors) + "->  " + toChar(975) + " - "
									+ coreptLib.getErrText(975) + chr(10);

						}

					}

				}

				for (int K = 0; K <= vNlegs; K++) {
					lFirstProcDesignMagVar = vLegs.get(1).getProcDesignMagVar();
					if (Objects.equals(nvl(rseg.getProcDesignMagVarInd(), "$"), "P") && i > 1
							&& !Objects.equals(lFirstProcDesignMagVar, null)) {
						if (!Objects.equals(nvl(vLegs.get(K).getProcDesignMagVar(), lFirstProcDesignMagVar),
								lFirstProcDesignMagVar)) {
							if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", 1190)) {
								vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vLegs.get(K).getSequenceNum()
										+ " - " + toChar(1190) + " - " + coreptLib.getErrText(1190) + chr(10);

							}

							lFirstProcDesignMagVar = null;
							// TODO EXIT

						}

					}

				}
				if (vNlegs > 1) {
					BigDecimal bigDecimal = app.executeFunction(BigDecimal.class, "CPT", "V_FIRST_LEG", "CHKPRC_UTIL",
							OracleTypes.NUMBER,
							new ProcedureInParameter("p_1stpt", vLegs.get(1).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_2ndpt", vLegs.get(2).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastpt", vLegs.get(vNlegs).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_route", vSegs.get(i).getRouteType(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_trid", vSegs.get(i).getTransitionIdent(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_legs", vNlegs, OracleTypes.NUMBER),
							new ProcedureInParameter("p_proc", pProc, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_is_map", vIsMap, OracleTypes.PLSQL_BOOLEAN),
							new ProcedureInParameter("p_1stwpd", vLegs.get(1).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastwpd", vLegs.get(vNlegs).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_pro_type", null, OracleTypes.VARCHAR));

					vErr = bigDecimal.intValue();
					// TODO v_err := chkprc_util.v_first_leg (vLegs.get(1).path_and_termination,
					// vLegs.get(2).path_and_termination, vLegs.get(v_nlegs).path_and_termination,
					// v_segs(i).route_type, v_segs(i).transition_ident, v_nlegs, p_proc,v_is_map,
					// vLegs.get(1).waypoint_desc_code, vLegs.get(v_nlegs).waypoint_desc_code,null)
					if (vErr > 0 && !coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErr)) {
						vAllErrors = getNullClean(vAllErrors) + "->  " + toChar(vErr) + " - "
								+ coreptLib.getErrText(vErr) + chr(10);

					}

				}

				else {
					BigDecimal bigDecimal = app.executeFunction(BigDecimal.class, "CPT", "V_FIRST_LEG", "CHKPRC_UTIL",
							OracleTypes.NUMBER,
							new ProcedureInParameter("p_1stpt", vLegs.get(1).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_2ndpt", vLegs.get(1).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastpt", vLegs.get(vNlegs).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_route", vSegs.get(i).getRouteType(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_trid", vSegs.get(i).getTransitionIdent(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_legs", vNlegs, OracleTypes.NUMBER),
							new ProcedureInParameter("p_proc", pProc, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_is_map", vIsMap, OracleTypes.PLSQL_BOOLEAN),
							new ProcedureInParameter("p_1stwpd", vLegs.get(1).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastwpd", vLegs.get(vNlegs).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_pro_type", null, OracleTypes.VARCHAR));

					vErr = bigDecimal.intValue();
					// TODO v_err := chkprc_util.v_first_leg (vLegs.get(1).path_and_termination,
					// vLegs.get(1).path_and_termination, vLegs.get(v_nlegs).path_and_termination,
					// v_segs(i).route_type, v_segs(i).transition_ident, v_nlegs, p_proc,v_is_map,
					// vLegs.get(1).waypoint_desc_code, vLegs.get(v_nlegs).waypoint_desc_code,null)
					if (vErr > 0 && !coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErr)) {
						vAllErrors = getNullClean(vAllErrors) + "->  " + toChar(vErr) + " - Single Leg, "
								+ coreptLib.getErrText(vErr) + chr(10);

					}

				}
				if (vNlegs > 1) {

					BigDecimal bigDecimal = app.executeFunction(BigDecimal.class, "CPT", "V_LAST_LEG", "CHKPRC_UTIL",
							OracleTypes.NUMBER,
							new ProcedureInParameter("p_1stpt", vLegs.get(1).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_2ndpt", vLegs.get(2).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastpt", vLegs.get(vNlegs).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_route", vSegs.get(i).getRouteType(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_trid", vSegs.get(i).getTransitionIdent(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_legs", vNlegs, OracleTypes.NUMBER),
							new ProcedureInParameter("p_proc", pProc, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_is_map", false, OracleTypes.PLSQL_BOOLEAN),
							new ProcedureInParameter("p_1stwpd", vLegs.get(1).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastwpd", vLegs.get(vNlegs).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_pro_type", null, OracleTypes.VARCHAR));
					vErr = bigDecimal.intValue();
					// TODO v_err := chkprc_util.v_last_leg (vLegs.get(1).path_and_termination,
					// vLegs.get(2).path_and_termination, vLegs.get(v_nlegs).path_and_termination,
					// v_segs(i).route_type, v_segs(i).transition_ident, v_nlegs, p_proc,v_is_map,
					// vLegs.get(1).waypoint_desc_code, vLegs.get(v_nlegs).waypoint_desc_code)
					if (vErr > 0 && !coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErr)) {
						vAllErrors = getNullClean(vAllErrors) + "->  " + toChar(vErr) + " - "
								+ coreptLib.getErrText(vErr) + chr(10);

					}

				}

				else {
					BigDecimal bigDecimal = app.executeFunction(BigDecimal.class, "CPT", "V_LAST_LEG", "CHKPRC_UTIL",
							OracleTypes.NUMBER,
							new ProcedureInParameter("p_1stpt", vLegs.get(1).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_2ndpt", vLegs.get(1).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastpt", vLegs.get(vNlegs).getPathAndTermination(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_route", vSegs.get(i).getRouteType(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_trid", vSegs.get(i).getTransitionIdent(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_legs", vNlegs, OracleTypes.NUMBER),
							new ProcedureInParameter("p_proc", pProc, OracleTypes.VARCHAR),
							new ProcedureInParameter("p_is_map", false, OracleTypes.PLSQL_BOOLEAN),
							new ProcedureInParameter("p_1stwpd", vLegs.get(1).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_lastwpd", vLegs.get(vNlegs).getWaypointDescCode(),
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_pro_type", null, OracleTypes.VARCHAR));
					vErr = bigDecimal.intValue();
					// TODO v_err := chkprc_util.v_last_leg (vLegs.get(1).path_and_termination,
					// vLegs.get(1).path_and_termination, vLegs.get(v_nlegs).path_and_termination,
					// v_segs(i).route_type, v_segs(i).transition_ident, v_nlegs, p_proc,v_is_map,
					// vLegs.get(1).waypoint_desc_code, vLegs.get(v_nlegs).waypoint_desc_code)
					if (vErr > 0 && !coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErr)) {
						vAllErrors = getNullClean(vAllErrors) + "->  " + toChar(vErr) + " - Single Leg, "
								+ coreptLib.getErrText(vErr) + chr(10);

					}

					if (!Objects.equals(rtrim(vLegs.get(1).getAlt1()), null)) {
						vAltitude = "Y";

					}

				}
				Struct[] structs = new Struct[vNlegs + 1];
				try (Connection conn = app.getDataSource().getConnection()) {
					structs[0] = conn.createStruct("CHECK_PROCEDURES.LEGREC", mapToObjectArray(new LegRec()));
					for (int cursor = 1; cursor <= vNlegs; cursor++) {
						structs[cursor] = conn.createStruct("CHECK_PROCEDURES.LEGREC",
								mapToObjectArray(vLegs.get(cursor)));
					}
				}
				Map<String, Object> ou = app.executeProcedure("CPT", "V_FIELDS_EXIST", "CHKPRC_UTIL",
						new ProcedureInParameter("p_proc", "STAR", OracleTypes.VARCHAR),
						new ProcedureInParameter("p_legs", structs, OracleTypes.ARRAY, "CHECK_PROCEDURES.LEGS_TABLE"),
						new ProcedureInParameter("p_nlegs", vNlegs, OracleTypes.NUMBER),
						new ProcedureOutParameter("p_errlist", OracleTypes.ARRAY, "CPT_TYPES.ERRLIST_TYPE"));

				Array errRes = (Array) ou.get("p_errlist");
				BigDecimal[] bg = (BigDecimal[]) errRes.getArray();
				vErrList.clear();
				for (BigDecimal itr1 : bg) {
					vErrList.add(itr1.intValue());
				}

				// TODO chkprc_util.v_fields_exist('STAR',vLegs.get,v_nlegs,v_errlist);
				if (vErrList.size() > 0) {
					for (int k = 0; k < vErrList.size(); k++) {
						vErrNum = toInteger(toChar(vErrList.get(k)).substring(0, 4));
						vSeq = vLegs.get(toInteger(substr(toChar(vErrList.get(k)), 5))).getSequenceNum();
						if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR", vErrNum)) {
							vAllErrors = getNullClean(vAllErrors) + "->  Seq#" + vSeq + " - " + toChar(vErrNum) + " - "
									+ coreptLib.getErrText(vErrNum) + chr(10);

						}

						if (length(vAllErrors) > 7000) {
							vAllErrors = getNullClean(vAllErrors) + "....";
							// TODO exit

						}

					}

				}

				if (!Objects.equals(vAllErrors, null)) {
					lsProcErr = getNullClean(lsProcErr) + chr(10) + "* Route Type '" + vSegs.get(i).getRouteType()
							+ "', Transition '" + vSegs.get(i).getTransitionIdent() + "': " + chr(10) + vAllErrors;
					vAllErrors = null;

				}

			}
			Map<String, Object> procall = app.executeProcedure("CPT", "CHECK_EXIST_ROUTE_TYPES", "CHKPRC_UTIL",
					new ProcedureInParameter("p_proc", "STAR", OracleTypes.VARCHAR),
					new ProcedureInParameter("p_exist_route", vExitRoute, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_seg_transition", segTransition, OracleTypes.VARCHAR),
					new ProcedureInParameter("p_seg_route_type", segRouteType, OracleTypes.VARCHAR),
					new ProcedureOutParameter("p_errlist", OracleTypes.ARRAY, "CPT_TYPES.ERRLIST_TYPE"),
					new ProcedureOutParameter("p_error", OracleTypes.NUMBER));

			Array errRes = (Array) procall.get("p_errlist");
			BigDecimal[] bg = (BigDecimal[]) errRes.getArray();
			vErrList.clear();
			for (BigDecimal itr1 : bg) {
				vErrList.add(itr1.intValue());
			}
			BigDecimal bigdecimal = (BigDecimal) procall.get("p_error");

			// TODO
			vErr = bigdecimal != null ? bigdecimal.intValue() : null;

			// TODO
			// chkprc_util.check_exist_route_types('STAR',v_exist_route,seg_transition,seg_route_type,v_errlist,v_err);
			if (vErr != null) {

				if (vErr > 0) {
					for (int i = 0; i < vErrList.size(); i++) {
						if (!coreptLib.isOverride(global.getDataSupplier(), vProcessingCycle, "STAR",
								vErrList.get(i))) {
							lsProcErr = "* " + toChar(vErrList.get(i)) + " - " + coreptLib.getErrText(vErrList.get(i))
									+ chr(10) + lsProcErr;

						}

					}

				}
			}

			if (Objects.equals(vCommonAircraftType, null)) {
				vTransitionAircraftType = vEnrtTransitionAircraftTyp;
				vRwyTransitionAircraftType = replace(vRwyTransitionAircraftType, ',', null);
				vTransitionAircraftType = replace(vTransitionAircraftType, ',', null);
				vInvalidValues = vTransitionAircraftType;
				for (int i = 0; i < toInteger(nvl(length(vRwyTransitionAircraftType), 0)); i++) {

					vCommonAircraftType = substr(vRwyTransitionAircraftType, i, 1);

					// TODO v_invalid_aircraft_type :=
					// chkprc_util.check_aircraft_type_comb(v_transition_aircraft_type,
					// v_common_aircraft_type, v_invalid_values)
					vInvalidAircraftType = app.executeFunction(String.class, "CPT", "CHECK_AIRCRAFT_TYPE_COMB",
							"CHKPRC_UTIL", OracleTypes.VARCHAR,
							new ProcedureInParameter("p_transition_aircraft_type", vTransitionAircraftType,
									OracleTypes.VARCHAR),
							new ProcedureInParameter("p_common_aircraft_type", vCommonAircraftType,
									OracleTypes.VARCHAR),
							new ProcedureInOutParameter("p_invalid_values", vInvalidValues, OracleTypes.VARCHAR));

				}
				if (Objects.equals(vInvalidAircraftType, "Y") && !Objects.equals(vInvalidValues, null)) {
					Boolean isOverride = app.executeFunction(Boolean.class, "CPT", "IS_OVERRIDE_ERROR", "UTIL1",
							OracleTypes.PLSQL_BOOLEAN,
							new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_cycle", vProcessingCycle, OracleTypes.NUMBER),
							new ProcedureInParameter("p_data_type", "STAR", OracleTypes.VARCHAR),
							new ProcedureInParameter("p_err_id", 5678, OracleTypes.NUMBER));
					if (!isOverride) {
						lsProcErr = "*5678 - " + coreptLib.getErrText(5678) + chr(10) + lsProcErr;

					}

				}

			}

			else {
				vTransitionAircraftType = vEnrtTransitionAircraftTyp + vRwyTransitionAircraftType;
				vTransitionAircraftType = replace(vTransitionAircraftType, ',', null);
				vInvalidAircraftType = app.executeFunction(String.class, "CPT", "CHECK_AIRCRAFT_TYPE_COMB",
						"CHKPRC_UTIL", OracleTypes.VARCHAR,
						new ProcedureInParameter("p_transition_aircraft_type", vTransitionAircraftType,
								OracleTypes.VARCHAR),
						new ProcedureInParameter("p_common_aircraft_type", vCommonAircraftType, OracleTypes.VARCHAR),
						new ProcedureInOutParameter("p_invalid_values", vInvalidValues, OracleTypes.VARCHAR));
				// TODO v_invalid_aircraft_type :=
				// chkprc_util.check_aircraft_type_comb(v_transition_aircraft_type,
				// v_common_aircraft_type, v_invalid_values)
				if (Objects.equals(vInvalidAircraftType, "Y")) {
					Boolean isOverride = app.executeFunction(Boolean.class, "CPT", "IS_OVERRIDE_ERROR", "UTIL1",
							OracleTypes.PLSQL_BOOLEAN,
							new ProcedureInParameter("p_supplier", global.getDataSupplier(), OracleTypes.VARCHAR),
							new ProcedureInParameter("p_cycle", vProcessingCycle, OracleTypes.NUMBER),
							new ProcedureInParameter("p_data_type", "STAR", OracleTypes.VARCHAR),
							new ProcedureInParameter("p_err_id", 5680, OracleTypes.NUMBER));
					if (!isOverride) {
						lsProcErr = "*5680 - " + coreptLib.getErrText(5680) + chr(10) + lsProcErr;

					}

				}

			}
			if (length(lsProcErr) < 20000) {
				pErrorList = lsProcErr;

			}

			else if (length(lsProcErr) >= 20000) {
				pErrorList = substr(lsProcErr, 1, 20000);

			}

			log.info("vOneProc Executed Successfully");
		} catch (vException e) {
			pErrorList = lsProcErr;

		} catch (Exception e) {

			coreptLib.dspMsg(e.getMessage());

		}
		return pErrorList;
	}

	// special method for handling relations
	private void setrelation() throws Exception {
		try {
			String masterBlock = system.getCursorBlock();
			Integer cursor = system.getCursorRecordIndex();
			Object relation = nameIn(this, system.getCursorBlock() + ".relations");
			if (relation instanceof HashMap<?, ?>) {
				HashMap<String, String> relations = (HashMap<String, String>) relation;
				for (Map.Entry<String, String> entry : relations.entrySet()) {
					system.setCursorRecordIndex(0);
					String key = entry.getKey();
					String value = entry.getValue();
					// String value = relations.get(key);
					for (String relationfield : value.split(",")) {
						copy(this, nameIn(this, masterBlock + "." + relationfield.trim()),
								key + "." + relationfield.trim());

					}
					system.setCursorBlock(key);
					try {
						coreptLib.coreptexecutequery(this);
					} catch (FormTriggerFailureException ex) {

						this.event.remove(event.size() - 1);

					}
					Block<?> block = (Block<?>) nameIn(this, system.getCursorBlock());
					for (system.setCursorRecordIndex(0); system.getCursorRecordIndex() < block.getData().size(); system
							.setCursorRecordIndex(system.getCursorRecordIndex() + 1))
						callPostquery(system.getCursorBlock());

				}

			}
			system.setCursorBlock(masterBlock);
			system.setCursorRecordIndex(cursor);
		}

		catch (Exception ex) {
			throw ex;
		}
	}

	// method for calling postquery
	private void callPostquery(String CurrentBlock) throws Exception {
		try {
			switch (CurrentBlock) {

			case "PL_STD_STAR":
				plStdStarPostQuery();
				break;
			case "PL_STD_STAR_LEG":
				plStdStarLegPostQuery();
				break;
			case "PL_STD_STAR_SEGMENT":
				plStdStarSegmentPostQuery();
				break;
			case "PL_TLD_STAR":
				plTldStarPostQuery();
				break;
			case "PL_TLD_STAR_LEG":
				plTldStarLegPostQuery();
				break;
			case "PL_TLD_STAR_SEGMENT":
				plTldStarSegmentPostQuery();
				break;

			case "STD_STAR_LEG":
				stdStarLegPostQuery();
				break;

			case "TLD_STAR_LEG":
				tldStarLegPostQuery();
				break;

			default:
				break;
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	public String setValidateInd(String pOInd, String pNInd) throws Exception {
		log.info("setValidateInd Executing");
//		String query = "";
//		Record rec = null;
		try {

			if (Objects.equals(pOInd, "N")) {

				return substr(pNInd, 1, 1) + "N----";

			}

			else if (Objects.equals(pOInd, "W")) {

				return substr(pNInd, 1, 2) + "W---";

			}

			else if (Objects.equals(pOInd, "H")) {

				return substr(pNInd, 1, 3) + "H--";

			}

			else if (Objects.equals(pOInd, "S")) {

				return substr(pNInd, 1, 4) + "S-";

			}

			else if (Objects.equals(pOInd, "Y")) {

				return substr(pNInd, 1, 5) + "Y";

			}

			else {

				return pNInd;

			}

			// log.info("setValidateInd Executed Successfully");
		} catch (Exception e) {
			log.error("Error while executing setValidateInd" + e.getMessage());
			throw e;

		}
	}

	public fillSegsRes fillSegs(String pRecordType, List<SegRec> pSegs, Integer pNsegs) {
		Integer vIndex = 0;
		// pSegs = new ArrayList<>();
		// Map<String, Object> res = new HashMap<>();
		if (Objects.equals(pRecordType, "S")) {

			// TODO first_record;
			for (PlStdStarSegment segment : plStdStarSegment.getData()) {
				if (!Objects.equals(segment.getRouteType(), null)) {
					// TODO LOOP

					pSegs.add(new SegRec());
					pSegs.get(vIndex).setProcedureType("STAR");
					pSegs.get(vIndex).setRecordType(pRecordType);
					pSegs.get(vIndex).setTerminalType("A");
					pSegs.get(vIndex).setTransitionIdent(segment.getTransitionIdent());
					pSegs.get(vIndex).setRouteType(segment.getRouteType());
					pSegs.get(vIndex).setAircraftType(segment.getAircraftType());
					pSegs.get(vIndex).setQualifier1(nvl(segment.getQualifier1(), " "));
					pSegs.get(vIndex).setQualifier2(nvl(segment.getQualifier2(), " "));
					pSegs.get(vIndex).setProcedureIdent(plStdStar.getStarIdent());
					pSegs.get(vIndex).setTerminalIdent(plStdStar.getAirportIdent());
					pSegs.get(vIndex).setTerminalIcao(plStdStar.getAirportIcao());
					pSegs.get(vIndex).setProcDesignMagVarInd(nvl(segment.getProcDesignMagVarInd(), " "));

					// TODO END LOOP;
					vIndex++;
				}

				pNsegs = vIndex;
			}
			// TODO first_record;

		}

		else {

			// TODO first_record;
			for (PlTldStarSegment segment : plTldStarSegment.getData()) {
				// TODO first_record;
				if (!Objects.equals(segment.getRouteType(), null)) {
					// TODO LOOP
					{
						pSegs.add(new SegRec());
						pSegs.get(vIndex).setProcedureType("STAR");
						pSegs.get(vIndex).setRecordType(pRecordType);
						pSegs.get(vIndex).setTerminalType("A");
						pSegs.get(vIndex).setTransitionIdent(segment.getTransitionIdent());
						pSegs.get(vIndex).setRouteType(segment.getRouteType());
						pSegs.get(vIndex).setAircraftType(segment.getAircraftType());
						pSegs.get(vIndex).setQualifier1(nvl(segment.getQualifier1(), " "));
						pSegs.get(vIndex).setQualifier2(nvl(segment.getQualifier2(), " "));
						pSegs.get(vIndex).setProcedureIdent(plTldStar.getStarIdent());
						pSegs.get(vIndex).setTerminalIdent(plTldStar.getAirportIdent());
						pSegs.get(vIndex).setTerminalIcao(plTldStar.getAirportIcao());
						pSegs.get(vIndex).setProcDesignMagVarInd(nvl(segment.getProcDesignMagVarInd(), " "));

					}
					// TODO END LOOP;
					vIndex++;
				}

				pNsegs = vIndex;
			}

		}

		return new fillSegsRes(pSegs, pNsegs);

	}

	public Pair<List<LegRec>, Integer> fillLegs(String pRecordType, String pRt, String pTi, String pAt,
			List<LegRec> pLegs, Integer pNlegs) {
		// pLegs.add(new LegRec(pNlegs, pNlegs, pAt, pRt, pRecordType, pAt, pNlegs, pAt,
		// pTi, pRt, pAt, pAt, pAt, pAt, pAt, pTi, pNlegs, pAt, pAt, pAt, pTi, pTi, pTi,
		// pAt, pAt, pAt, pAt, pTi, pTi, pTi, pAt, pAt, pAt, pTi, pTi, null, pAt, pAt,
		// pNlegs, null, pNlegs, pAt, pTi, null, pRecordType, pAt, pRt, pTi));
		Integer vIndex = 0;
		pLegs = new ArrayList<>();
		if (Objects.equals(pRecordType, "S")) {
			for (PlStdStarLeg leg : plStdStarLeg.getData()) {
				if (Objects.equals(leg.getTransitionIdent(), pTi) && Objects.equals(leg.getRouteType(), pRt)
						&& Objects.equals(leg.getAircraftType(), pAt)) {
					pLegs.add(new LegRec());
					pLegs.get(vIndex).setProcedureType("STAR");
					pLegs.get(vIndex).setRecordType(pRecordType);
					pLegs.get(vIndex).setTerminalType("A");
					pLegs.get(vIndex).setTransitionIdent(leg.getTransitionIdent());
					pLegs.get(vIndex).setRouteType(leg.getRouteType());
					pLegs.get(vIndex).setAircraftType(leg.getAircraftType());
					pLegs.get(vIndex).setProcedureIdent(plStdStar.getStarIdent());
					pLegs.get(vIndex).setTerminalIdent(plStdStar.getAirportIdent());
					pLegs.get(vIndex).setTerminalIcao(plStdStar.getAirportIcao());
					pLegs.get(vIndex).setSequenceNum(leg.getSequenceNum());
					pLegs.get(vIndex).setAlt1(toString(leg.getAlt1()));
					pLegs.get(vIndex).setAlt2(toString(leg.getAlt2()));
					pLegs.get(vIndex).setAltDescription(leg.getAltDescription());
					pLegs.get(vIndex).setArcRadius(leg.getArcRadius());
					pLegs.get(vIndex).setAtcInd(leg.getAtcInd());
					pLegs.get(vIndex).setCenterFixIcaoCode(leg.getCenterFixIcaoCode());
					pLegs.get(vIndex).setCenterFixIdent(leg.getCenterFixIdent());
					pLegs.get(vIndex).setCenterFixMultipleCode(leg.getCenterFixMultipleCode());
					pLegs.get(vIndex).setCenterFixSection(leg.getCenterFixSection());
					pLegs.get(vIndex).setCenterFixSubsection(leg.getCenterFixSubsection());
					pLegs.get(vIndex).setCycleData(leg.getCycleData());
					pLegs.get(vIndex).setFileRecno(toString(leg.getFileRecno()));
					pLegs.get(vIndex).setFixIcaoCode(leg.getFixIcaoCode());
					pLegs.get(vIndex).setFixIdent(leg.getFixIdent());
					pLegs.get(vIndex).setFixSectionCode(leg.getFixSectionCode());
					pLegs.get(vIndex).setFixSubsectionCode(leg.getFixSubsectionCode());
					pLegs.get(vIndex).setMagneticCourse(leg.getMagneticCourse());
					pLegs.get(vIndex).setPathAndTermination(leg.getPathAndTermination());
					pLegs.get(vIndex).setRecommNavaidIcaoCode(leg.getRecommNavaidIcaoCode());
					pLegs.get(vIndex).setRecommNavaidIdent(leg.getRecommNavaidIdent());
					pLegs.get(vIndex).setRecommNavaidSection(leg.getRecommNavaidSection());
					pLegs.get(vIndex).setRecommNavaidSubsection(leg.getRecommNavaidSubsection());
					pLegs.get(vIndex).setRho(leg.getRho());
					pLegs.get(vIndex).setRnp(leg.getRnp());
					pLegs.get(vIndex).setRouteDistance(leg.getRouteDistance());
					pLegs.get(vIndex).setSpeedLimit(toInteger(leg.getSpeedLimit()));
					pLegs.get(vIndex).setTheta(leg.getTheta());
					pLegs.get(vIndex).setTransAltitude(toInteger(leg.getTransAltitude()));
					pLegs.get(vIndex).setTurnDirValid(leg.getTurnDirValid());
					pLegs.get(vIndex).setTurnDirection(leg.getTurnDirection());
					pLegs.get(vIndex).setVerticalAngle(leg.getVerticalAngle());
					pLegs.get(vIndex).setWaypointDescCode(leg.getWaypointDescCode());
					pLegs.get(vIndex).setProcDesignMagVar(leg.getProcDesignMagVar());
					vIndex++;
				}

				if (Objects.equals(system.getLastRecord(), true)) {

					// TODO first_record;
					// TODO exit

				}

				else {
					nextRecord("");

				}

			}
			// TODO END LOOP;
			pNlegs = vIndex;

		}
		// TODO first_record;
		// TODO LOOP

		else {
			for (PlTldStarLeg leg : plTldStarLeg.getData()) {
				if (Objects.equals(leg.getTransitionIdent(), pTi) && Objects.equals(leg.getRouteType(), pRt)
						&& Objects.equals(leg.getAircraftType(), pAt)) {
					pLegs.add(new LegRec());
					pLegs.get(vIndex).setProcedureType("Star");
					pLegs.get(vIndex).setRecordType(pRecordType);
					pLegs.get(vIndex).setTerminalType("A");
					pLegs.get(vIndex).setTransitionIdent(leg.getTransitionIdent());
					pLegs.get(vIndex).setRouteType(leg.getRouteType());
					pLegs.get(vIndex).setAircraftType(leg.getAircraftType());
					pLegs.get(vIndex).setProcedureIdent(plTldStar.getStarIdent());
					pLegs.get(vIndex).setTerminalIdent(plTldStar.getAirportIdent());
					pLegs.get(vIndex).setTerminalIcao(plTldStar.getAirportIcao());
					pLegs.get(vIndex).setSequenceNum(leg.getSequenceNum());
					pLegs.get(vIndex).setAlt1(leg.getAlt1());
					pLegs.get(vIndex).setAlt2(leg.getAlt2());
					pLegs.get(vIndex).setAltDescription(leg.getAltDescription());
					pLegs.get(vIndex).setArcRadius(leg.getArcRadius());
					pLegs.get(vIndex).setAtcInd(leg.getAtcInd());
					pLegs.get(vIndex).setCenterFixIcaoCode(leg.getCenterFixIcaoCode());
					pLegs.get(vIndex).setCenterFixIdent(leg.getCenterFixIdent());
					pLegs.get(vIndex).setCenterFixMultipleCode(leg.getCenterFixMultipleCode());
					pLegs.get(vIndex).setCenterFixSection(leg.getCenterFixSection());
					pLegs.get(vIndex).setCenterFixSubsection(leg.getCenterFixSubsection());
					pLegs.get(vIndex).setCycleData(leg.getCycleData());
					pLegs.get(vIndex).setFileRecno(toString(leg.getFileRecno()));
					pLegs.get(vIndex).setFixIcaoCode(leg.getFixIcaoCode());
					pLegs.get(vIndex).setFixIdent(leg.getFixIdent());
					pLegs.get(vIndex).setFixSectionCode(leg.getFixSectionCode());
					pLegs.get(vIndex).setFixSubsectionCode(leg.getFixSubsectionCode());
					pLegs.get(vIndex).setMagneticCourse(leg.getMagneticCourse());
					pLegs.get(vIndex).setPathAndTermination(leg.getPathAndTermination());
					pLegs.get(vIndex).setRecommNavaidIcaoCode(leg.getRecommNavaidIcaoCode());
					pLegs.get(vIndex).setRecommNavaidIdent(leg.getRecommNavaidIdent());
					pLegs.get(vIndex).setRecommNavaidSection(leg.getRecommNavaidSection());
					pLegs.get(vIndex).setRecommNavaidSubsection(leg.getRecommNavaidSubsection());
					pLegs.get(vIndex).setRho(leg.getRho());
					pLegs.get(vIndex).setRnp(leg.getRnp());
					pLegs.get(vIndex).setRouteDistance(leg.getRouteDistance());
					pLegs.get(vIndex).setSpeedLimit(toInteger(leg.getSpeedLimit()));
					pLegs.get(vIndex).setTheta(leg.getTheta());
					pLegs.get(vIndex).setTransAltitude(toInteger(leg.getTransAltitude()));
					pLegs.get(vIndex).setTurnDirValid(leg.getTurnDirValid());
					pLegs.get(vIndex).setTurnDirection(leg.getTurnDirection());
					pLegs.get(vIndex).setVerticalAngle(leg.getVerticalAngle());
					pLegs.get(vIndex).setWaypointDescCode(leg.getWaypointDescCode());
					pLegs.get(vIndex).setProcDesignMagVar(leg.getProcDesignMagVar());
					vIndex++;
				}

				if (Objects.equals(system.getLastRecord(), true)) {

					// TODO first_record;
					// TODO exit

				}

				else {
					nextRecord("");

				}

			}
			// TODO END LOOP;
			pNlegs = vIndex;

		}
		// TODO first_record;
		// TODO LOOP

		return Pair.of(pLegs, pNlegs);
	}

	@Override

	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyDelrec(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyDelrec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);

			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			}

			else {
				if (!Arrays.asList("NEW", "INSERT").contains(system.getRecordStatus())) {
					parameter.setUpdRec(coreptLib.setActionRestr(toChar(nameIn(this, "system.cursor_block")),
							global.getDataSupplier(), toInteger(global.getProcessingCycle()),
							toInteger(global.getDcrNumber()), parameter.getRecordType(), "DEL"));
					String pTableType = "M2C";
					String vBlock = system.getCursorBlock();
					String query = null;
					Integer vDcrNumber = toInteger(nameIn(this, substr(vBlock, 1, 11) + ".create_dcr_number"));
					Integer vProcessingCycle = toInteger(nameIn(this, vBlock + ".processing_cycle"));
					String vValidateInd = toChar(nameIn(this, substr(vBlock, 1, 11) + ".validate_ind"));
					String vStatus = system.getFormStatus();
					String lsReturn = null;
					String plStdStarSegmentCur = """
							SELECT 1 FROM PL_STD_STAR_SEGMENT P
							WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
							P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
							""";
					String plStdStarLegCur = """
							SELECT 1 FROM PL_STD_STAR_LEG P
							WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
							P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
							""";
					String plTldStarSegmentCur = """
							SELECT 1 FROM PL_TLD_STAR_SEGMENT P
							WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
							P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.customer_ident = ?
							""";
					String plTldStarLegCur = """
							SELECT 1 FROM PL_TLD_STAR_LEG P
							WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
							P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.customer_ident = ?
							""";
					String segmentPlStdLegCur = """
							SELECT 1 FROM PL_STD_STAR_LEG P
							WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
							P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.ROUTE_TYPE = ? and
							p.TRANSITION_IDENT = ?
							""";
					String segmentPlTldLegCur = """
							SELECT 1 FROM PL_TLD_STAR_LEG P
							WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
							P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? AND P.CUSTOMER_IDENT = ? and
							P.ROUTE_TYPE = ? and p.TRANSITION_IDENT = ?
							""";
					// String dummyDefine = null;
					String vExistMessage = null;
					Integer vButton = null;

					if (Objects.equals(vStatus, "CHANGED")) {
						// TODO v_validate_ind :=
						// refresh_ml_utilities.get_validate_ind(substr(v_block,1,11),v_dcr_number)
						vValidateInd = app.executeFunction(String.class, "CPTM", "Get_validate_ind",
								"refresh_ml_utilities", OracleTypes.VARCHAR,
								new ProcedureInParameter("p_table", substr(vBlock, 1, 11), OracleTypes.VARCHAR),
								new ProcedureInParameter("p_dcr", vDcrNumber, OracleTypes.NUMBER));
					}

					if (Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {
						if (Objects.equals(refreshMasterLibrary.checkReferenceInfo(vBlock, "D"), "N")) {
							throw new FormTriggerFailureException(event);

						}

					}

					if (Objects.equals(vBlock, "PL_STD_STAR")) {
						List<Record> plStdStarSeg = app.executeQuery(plStdStarSegmentCur, plStdStar.getAirportIdent(),
								plStdStar.getAirportIcao(), plStdStar.getStarIdent(), plStdStar.getDataSupplier(),
								plStdStar.getProcessingCycle());
						// TODO FETCHPL_STD_STAR_SEGMENT_curINTODummy_Define
						if ((plStdStarSeg.size() > 0)) {
							vExistMessage = "There are existing segments and/or legs.";

						}

						else {
							List<Record> plStdStarLeg = app.executeQuery(plStdStarLegCur, plStdStar.getAirportIdent(),
									plStdStar.getAirportIcao(), plStdStar.getStarIdent(), plStdStar.getDataSupplier(),
									plStdStar.getProcessingCycle());
							// TODO FETCHPL_STD_STAR_LEG_curINTODummy_Define
							if ((plStdStarLeg.size() > 0)) {
								vExistMessage = "There are existing legs.";

							}

							// TODO CLOSEPL_STD_STAR_LEG_cur

						}
						// TODO CLOSEPL_STD_STAR_SEGMENT_cur

					}

					else if (Objects.equals(vBlock, "PL_TLD_STAR")) {
						List<Record> plTldStarSegCur = new ArrayList<>();
						plTldStarSegCur = app.executeQuery(plTldStarSegmentCur, plTldStar.getAirportIdent(),
								plTldStar.getAirportIcao(), plTldStar.getStarIdent(), plTldStar.getDataSupplier(),
								plTldStar.getProcessingCycle(), plTldStar.getCustomerIdent());
						// TODO FETCHPL_TLD_STAR_SEGMENT_curINTODummy_Define
						if ((plTldStarSegCur.size() > 0)) {
							vExistMessage = "There are existing segments and/or legs.";

						}

						else {
							List<Record> plTldStarLeg = new ArrayList<>();
							if (plTldStarLeg != null) {
								log.info(" " + plTldStarLeg);
							}
							plTldStarLeg = app.executeQuery(plTldStarLegCur, plTldStar.getAirportIdent(),
									plTldStar.getAirportIcao(), plTldStar.getStarIdent(), plTldStar.getDataSupplier(),
									plTldStar.getProcessingCycle(), plTldStar.getCustomerIdent());
							// TODO FETCHPL_TLD_STAR_LEG_curINTODummy_Define
							if ((plTldStarLeg.size() > 0)) {
								vExistMessage = "There are existing legs.";

							}

							// TODO CLOSEPL_TLD_STAR_LEG_cur

						}
						// TODO CLOSEPL_TLD_STAR_SEGMENT_cur

					}

					else if (Objects.equals(vBlock, "PL_STD_STAR_SEGMENT")) {
						List<Record> segmentPlStdLeg = new ArrayList<>();
						segmentPlStdLeg = app.executeQuery(segmentPlStdLegCur, plStdStar.getAirportIdent(),
								plStdStar.getAirportIcao(), plStdStar.getStarIdent(), plStdStar.getDataSupplier(),
								plStdStar.getProcessingCycle(),
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
								plStdStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
						// TODO FETCHSEGMENT_PL_STD_LEG_curINTODummy_Define
						if ((segmentPlStdLeg.size() > 0)) {
							vExistMessage = "There are existing legs.";

						}

						// TODO CLOSESEGMENT_PL_STD_LEG_cur

					}

					else if (Objects.equals(vBlock, "PL_TLD_STAR_SEGMENT")) {
						List<Record> segmentPlTldLeg = new ArrayList<>();
						segmentPlTldLeg = app.executeQuery(segmentPlTldLegCur, plTldStar.getAirportIdent(),
								plTldStar.getAirportIcao(), plTldStar.getStarIdent(), plTldStar.getDataSupplier(),
								plTldStar.getProcessingCycle(), plTldStar.getCustomerIdent(),
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType(),
								plTldStarSegment.getRow(system.getCursorRecordIndex()).getTransitionIdent());
						// TODO FETCHSEGMENT_PL_TLD_LEG_curINTODummy_Define
						if ((segmentPlTldLeg.size() > 0)) {
							vExistMessage = "There are existing legs.";

						}

						// TODO CLOSESEGMENT_PL_TLD_LEG_cur

					}

					if (Objects.equals(parameter.getRecordType(), "T")) {
						lsReturn = coreptLib.dcrEffectiveCycleFun(toInteger(plTldStar.getProcessingCycle()), "D");
						if (Objects.equals(lsReturn, "Y")) {
							parameter.setEffectiveDel("Y");

						}

					}

					if (Objects.equals(lsReturn, "N")) {
						throw new FormTriggerFailureException(event);

					}

					if (!Objects.equals(vExistMessage, null)) {
						if (like("%STAR", vBlock)) {
							// TODO v_button := DISPLAY_ALERT.MORE_BUTTONs('S','DELETE PROCEDURE',
							// v_exist_MESSAGE||chr(10)|| 'Pick your choice
							// carefully:'||chr(10)||chr(10),'Delete All','Cancel')
							alertDetails.getCurrent();
							if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
								vButton = moreButtons("C", "DELETE PROCEDURE",
										vExistMessage + chr(10) + "Pick your choice carefully:" + chr(10) + chr(10),
										"Delete All", "Cancel", "");
								alertDetails.createNewRecord("delPrc");
								throw new AlertException(event, alertDetails);
							} else {
								vButton = alertDetails.getAlertValue("delPrc", alertDetails.getCurrentAlert());
							}
							if (Objects.equals(vButton, 1)) {
								if (Objects.equals(vBlock, "PL_STD_STAR")) {

									query = """
											DELETE FROM PL_STD_STAR_SEGMENT P
											WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
											P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
											""";
									app.executeNonQuery(query, plStdStar.getAirportIdent(), plStdStar.getAirportIcao(),
											plStdStar.getStarIdent(), plStdStar.getDataSupplier(),
											plStdStar.getProcessingCycle());
									query = """
											DELETE FROM PL_STD_STAR_LEG P
											WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
											P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ?
											""";
									app.executeNonQuery(query, plStdStar.getAirportIdent(), plStdStar.getAirportIcao(),
											plStdStar.getStarIdent(), plStdStar.getDataSupplier(),
											plStdStar.getProcessingCycle());
								}

								else if (Objects.equals(vBlock, "PL_TLD_STAR")) {

									query = """
											DELETE FROM PL_TLD_STAR_SEGMENT P
											WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
											P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.CUSTOMER_IDENT = ?
											""";
									app.executeNonQuery(query, plTldStar.getAirportIdent(), plTldStar.getAirportIcao(),
											plTldStar.getStarIdent(), plTldStar.getDataSupplier(),
											plTldStar.getProcessingCycle(), plTldStar.getCustomerIdent());
									query = """
											DELETE FROM PL_TLD_STAR_LEG P
											WHERE P.AIRPORT_IDENT = ? and P.AIRPORT_ICAO = ? and P.STAR_IDENT = ? and
											P.DATA_SUPPLIER = ? and P.PROCESSING_CYCLE = ? and P.CUSTOMER_IDENT = ?
											""";
									app.executeNonQuery(query, plTldStar.getAirportIdent(), plTldStar.getAirportIcao(),
											plTldStar.getStarIdent(), plTldStar.getDataSupplier(),
											plTldStar.getProcessingCycle(), plTldStar.getCustomerIdent());
								}

							}

							else {
								parameter.setEffectiveDel("N");
								throw new FormTriggerFailureException(event);

							}

						}

						else if (like("%SEGMENT", vBlock)) {
							// TODO v_button := DISPLAY_ALERT.MORE_BUTTONs('S','DELETE PROCEDURE',
							// v_exist_MESSAGE||chr(10)|| 'Pick your choice
							// carefully:'||chr(10)||chr(10),'Delete Legs too','Delete Segment
							// only','Cancel')

							alertDetails.getCurrent();
							if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
								vButton = moreButtons("C", "DELETE PROCEDURE",
										vExistMessage + chr(10) + chr(10) + "Pick your choice carefully:" + chr(10),
										"Delete Legs too", "Delete Segment only", "Cancel");
								alertDetails.createNewRecord("delProc");
								throw new AlertException(event, alertDetails);
							} else {
								vButton = alertDetails.getAlertValue("delProc", alertDetails.getCurrentAlert());
							}

							if (Objects.equals(vButton, 1)) {
								if (Objects.equals(vBlock, "PL_STD_STAR_SEGMENT")) {

									// TODO first_record;
									for (PlStdStarLeg plStdStarLeg : plStdStarLeg.getData()) {
										if (Objects.equals(plStdStarLeg.getRouteType(),
												plStdStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())
												&& Objects.equals(plStdStarLeg.getTransitionIdent(), plStdStarSegment
														.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {
											if (Objects.equals(plStdStarLeg.getRefInfo(), null)) {
												parameter.setUpdDcrDel("Y");
												deleteRecord("");
												plStdStarLeg.setRecordStatus("DELETED");

											}

											else {
												controlBlock.setStdValidationErrors(
														"The leg with seq# " + plStdStarLeg.getSequenceNum()
																+ " of the seg: " + plStdStarLeg.getRefInfo());
												throw new FormTriggerFailureException(event);

											}

										}

									}

									// TODO first_record;
									setItemProperty("control_block.std_leg_errors", VISIBLE, PROPERTY_FALSE);

								}

								else if (Objects.equals(vBlock, "PL_TLD_STAR_SEGMENT")) {

									// TODO first_record;
									for (PlTldStarLeg plTldStarLeg : plTldStarLeg.getData()) {
										if (Objects.equals(plTldStarLeg.getRouteType(),
												plTldStarSegment.getRow(system.getCursorRecordIndex()).getRouteType())
												&& Objects.equals(plTldStarLeg.getTransitionIdent(), plTldStarSegment
														.getRow(system.getCursorRecordIndex()).getTransitionIdent())) {
											if (Objects.equals(plTldStarLeg.getRefInfo(), null)) {
												parameter.setUpdDcrDel("Y");
												// deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
												// deleteRecord("PL_TLD_STAR_SEGMENT");
												deleteRecord("");
												plTldStarLeg.setRecordStatus("DELETED");

											}

											else {
												controlBlock.setTldValidationErrors(
														"The leg with seq# " + plTldStarLeg.getSequenceNum()
																+ " of the seg: " + plTldStarLeg.getRefInfo());
												throw new FormTriggerFailureException(event);

											}

										}

										else {

										}

									}

									setItemProperty("control_block.tld_leg_errors", VISIBLE, PROPERTY_FALSE);

								}

							}

							else if (Objects.equals(vButton, 3)) {
								parameter.setEffectiveDel("N");
								throw new FormTriggerFailureException(event);

							}

						}

					}

					else {
						if (like("%LEG", vBlock)) {

							alertDetails.getCurrent();
							if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {
								vButton = moreButtons("C", "DELETE A LEG",
										"Please be sure that you want to delete this leg." + chr(10) + chr(10),
										"Delete", "Cancel", "");
								alertDetails.createNewRecord("delLeg");
								parameter.setEffectiveDel("N");
								throw new AlertException(event, alertDetails);
							} else {
								vButton = alertDetails.getAlertValue("delLeg", alertDetails.getCurrentAlert());
							}

							if (Objects.equals(vButton, 2)) {
								throw new FormTriggerFailureException(event);

							}

						}

					}
					if (Arrays.asList("Y", "S", "H", "O").contains(vValidateInd) && like("%STAR", vBlock)) {

						// TODO refresh_master_library.delete_from_ref_table(v_dcr_number);
						refreshMasterLibrary.deleteFromRefTable(vDcrNumber, null);
					}

					if (like("%STAR", upper(vBlock))) {
						String blocks = null;
						if (Objects.equals(upper(vBlock), "PL_STD_STAR")) {
							blocks = "plStdStarLeg,plStdStarSegment";
						} else {
							blocks = "plTldStarLeg,plTldStarSegment";
						}
						String[] blockarr = blocks.split(",");
						for (String block : blockarr) {
							Object obj1 = OracleHelpers.findBlock(this, block);

							if (obj1 instanceof Block<?>) {
								Block<?> blockss = (Block<?>) obj1;
								for (Object blockval : blockss.getData()) {
									if (Objects.equals(nameIn(blockval, "record_Status"), "CHANGED")) {
										OracleHelpers.copy(blockval, "QUERIED", "recordStatus");
									}
								}
							}
						}
					}

					if (Objects.equals(global.getLibRefreshed(), "Y")
							&& Arrays.asList(global.getNewProcessingCycle(), global.getOldProcessingCycle())
									.contains(toChar(vProcessingCycle))
							&& Arrays.asList("Y", "S", "H", "O").contains(vValidateInd)) {
						if (like("%STAR", vBlock)) {

							coreptLib.dspMsg("System is going to refresh this deletion for the master\nlibrary table.");

							refreshMasterLibrary.refreshARecord(pTableType, vDcrNumber, vProcessingCycle,
									substr(vBlock, 4, 3) + "_STAR", "I", null);
							// deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
							copy("DELETED", system.getCursorBlock() + ".record_status");
							deleteRecord("");
							commitForm(this);
							message("Record has been saved successfully");
							String _rowid = toString(nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".rowid"));
							sendUpdatedRowIdDetails(_rowid);
							refreshMasterLibrary.setRecordGroup(vDcrNumber, "I", substr(vBlock, 4, 3) + "_STAR",
									vProcessingCycle, "D");
							parameter.setEffectiveDel("N");
							coreptLib.dspMsg("Refresh Successful and the deletion is commited.");

						}

						else {

							refreshMasterLibrary.setRecordGroup(vDcrNumber, vValidateInd, substr(vBlock, 1, 11),
									vProcessingCycle, "D");
							parameter.setUpdDcrDel("Y");
							copy("DELETED", system.getCursorBlock() + ".record_status");
							deleteRecord("");
							String _rowid = toString(nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".rowid"));
							sendLockRowIdDetails(_rowid);

							// deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));

						}

					}

					else {
						parameter.setUpdDcrDel("Y");
						copy("DELETED", system.getCursorBlock() + ".record_status");
						deleteRecord("");
						String _rowid = toString(nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".rowid"));
						sendLockRowIdDetails(_rowid);
						// deleteRecord(HoneyWellUtils.toCamelCase(system.getCursorBlock()));
						if (like("%STAR", upper(vBlock))) {
							parameter.setEffectiveDel("N");
							copy("DELETED", system.getCursorBlock() + ".record_status");

							commitForm(this);
							_rowid = toString(nameIn(this, substr(system.getCursorBlock(), 1, 11) + ".rowid"));
							sendUpdatedRowIdDetails(_rowid);
							message("Record has been saved successfully");

						}

					}

				}

				else {
					copy("DELETED", system.getCursorBlock() + ".record_status");

					deleteRecord("");
//					String _rowid =  toString(nameIn(this,substr(system.getCursorBlock(), 1,11)+".rowid"));
//					sendUpdatedRowIdDetails(_rowid);

				}

				controlBlock.setValidated("N");

			}

			this.plStdStarSegment.filterNonDeletedRecords();
			this.plStdStarLeg.filterNonDeletedRecords();
			this.plTldStarSegment.filterNonDeletedRecords();
			this.plTldStarLeg.filterNonDeletedRecords();
			mergeDelete();
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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> keyCrerec(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyCrerec Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			if (Objects.equals(parameter.getWorkType(), "VIEW")) {
				// null;

			}

			else {
				parameter.setUpdRec(coreptLib.setActionRestr(toChar(nameIn(this, "system.cursor_block")),
						global.getDataSupplier(), toInteger(global.getProcessingCycle()),
						toInteger(global.getDcrNumber()), parameter.getRecordType(), "CRE"));
				if (Objects.equals(parameter.getUpdRec(), "Y")) {
					if (Objects.equals(system.getCursorBlock(), "PL_STD_STAR_LEG")
							&& Objects.equals(system.getCursorRecordIndex(), 0) && Objects.equals(
									plStdStarLeg.getRow(system.getCursorRecordIndex()).getSequenceNum(), null)) {
//						goBlock("pl_std_sid_segment", "");
						int segIndex = 0;
						system.setCursorRecordIndex(0);
						// TODO first_record;
						if (!Objects.equals(plStdStarLeg.getRow(segIndex).getRouteType(), null)) {
//							goBlock("pl_std_sid_leg", "");
							plStdStarLeg.getRow(system.getCursorRecordIndex())
									.setAircraftType(plStdStarLeg.getRow(segIndex).getAircraftType());

						}
					} else if (Objects.equals(system.getCursorBlock(), "PL_TLD_STAR_LEG")
							&& Objects.equals(system.getCursorRecordIndex(), 0)) {
//						goBlock("pl_std_sid_segment", "");
						int segIndex = 0;
						system.setCursorRecordIndex(0);
						// TODO first_record;
						if (!Objects.equals(plTldStarSegment.getRow(segIndex).getRouteType(), null)) {
//							goBlock("pl_std_sid_leg", "");
							plTldStarSegment.getRow(system.getCursorRecordIndex())
									.setAircraftType(plTldStarSegment.getRow(segIndex).getAircraftType());

						}
					}
				}

			}
			setBlockProperty(system.getCursorBlock(), FormConstant.INSERT_ALLOWED, FormConstant.PROPERTY_TRUE);
			mergeDelete();
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" keyCrerec executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the keyCrerec Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> searchPlStdStar(
			AirportStarTriggerRequestDto reqDto, int page, int rec) throws Exception {
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();

		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			commonOncleardetail();

			PlStdStarQuerySearchDto plstdstarQuerySearch = new PlStdStarQuerySearchDto();
			OracleHelpers.setMappingValues(plStdStar, plstdstarQuerySearch);

			PlStdStarLegQuerySearchDto plstdstarlegQuerySearch = new PlStdStarLegQuerySearchDto();
			PlStdStarSegmentQuerySearchDto plstdstarsegmentQuerySearch = new PlStdStarSegmentQuerySearchDto();

			if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
				if (Objects.equals(toString(nameIn(this, "PL_STD_STAR.LAST_WHERE")), null))
					plstdstarQuerySearch.setDataSupplier(global.getDataSupplier());
				if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
					plstdstarQuerySearch.setProcessingCycle(global.getProcessingCycle());

				}

			}

			String sanitizedlastWhere = "";
			Long total = 0L;
			String searchQuerymas = "";
			List<Record> records = null;
			String countQuery = "";
			String lastWhere = toString(
					nameIn(nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock())), "lastWhere"));
			sanitizedlastWhere = app.sanitizeValueCheck(lastWhere);
//			Field _queryField = lastWhere;
//			if (_queryField != null) {
//				_queryField.setAccessible(true);
//				Object value = _queryField.get(plstdstarQuerySearch);
////					sanitizedqueryWhere = (String) value;
//				sanitizedlastWhere = app.sanitizeValueCheck((String) value);
//			}
			if (OracleHelpers.isNullorEmpty(sanitizedlastWhere)) {
				// Long total = 0L;
				// Total Count Process
				countQuery = app.getQuery(plstdstarQuerySearch, "pl_std_star", "",
						"airport_ident,airport_icao,star_ident,processing_cycle", true, false);
				Record record = app.selectInto(countQuery);
				total = record.getLong();
				searchQuerymas = app.getQuery(plstdstarQuerySearch, "pl_std_star", "",
						"airport_ident,airport_icao,star_ident,processing_cycle", false, false);

				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuerymas, offset, rec);

				for (Record searchRec : records) {

					plStdStar = app.mapResultSetToClass(searchRec, PlStdStar.class);
					// Coverity fix
//					plStdStar.getLastQuery();

					callPostquery("PL_STD_STAR");
				}
				plStdStar.setLastQuery(hashUtils.encrypt(searchQuerymas));
			} else {
				countQuery = app.getQuery(plstdstarQuerySearch, "pl_std_star", sanitizedlastWhere,
						"airport_ident,airport_icao,star_ident,processing_cycle", true, false);
				Record record = app.selectInto(app.sanitizeValueCheck(countQuery));
				total = record.getLong();
				searchQuerymas = app.getQuery(plstdstarQuerySearch, "pl_std_star", sanitizedlastWhere,
						"airport_ident,airport_icao,star_ident,processing_cycle", false, false);
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuerymas, offset, rec);

				for (Record searchRec : records) {

					plStdStar = app.mapResultSetToClass(searchRec, PlStdStar.class);

					callPostquery("PL_STD_STAR");
				}
				plStdStar.setLastQuery(hashUtils.encrypt(searchQuerymas));
			}

			// setting relation
			{

				plstdstarlegQuerySearch.setAirportIdent(plStdStar.getAirportIdent());
				plstdstarlegQuerySearch.setAirportIcao(plStdStar.getAirportIcao());
				plstdstarlegQuerySearch.setStarIdent(plStdStar.getStarIdent());
				plstdstarlegQuerySearch.setDataSupplier(plStdStar.getDataSupplier());
				plstdstarlegQuerySearch.setProcessingCycle(OracleHelpers.toString(plStdStar.getProcessingCycle()));

			}
			// countQuery = app.getQuery(plstdstarlegQuerySearch, "pl_Std_star_leg", "",
			// "route_type, transition_ident, sequence_num", true, true);
			// record = app.selectInto(countQuery);
			app.selectInto(countQuery);
			// total = record.getLong();
			String searchQueryleg = app.getQuery(plstdstarlegQuerySearch, "pl_Std_star_leg", "",
					"route_type, transition_ident, sequence_num", false, true);
			records = null;
			records = app.executeQuery(searchQueryleg);

			plStdStarLeg = new Block<PlStdStarLeg>();
//			int i = 0;
			for (Record searchRec : records) {
				plStdStarLeg.add(app.mapResultSetToClass(searchRec, PlStdStarLeg.class));
//				system.setCursorRecordIndex(i);
			}
			callPostquery("PL_STD_STAR_LEG");

			plStdStarLeg.setLastQuery(hashUtils.encrypt(searchQueryleg));

			{

				plstdstarsegmentQuerySearch.setAirportIdent(plStdStar.getAirportIdent());
				plstdstarsegmentQuerySearch.setAirportIcao(plStdStar.getAirportIcao());
				plstdstarsegmentQuerySearch.setStarIdent(plStdStar.getStarIdent());
				plstdstarsegmentQuerySearch.setDataSupplier(plStdStar.getDataSupplier());
				plstdstarsegmentQuerySearch.setProcessingCycle(OracleHelpers.toString(plStdStar.getProcessingCycle()));

			}

			String searchQueryseg = app.getQuery(plstdstarsegmentQuerySearch, "pl_Std_star_segment", "",
					"route_type, transition_ident", false, true);
			records = null;
			records = app.executeQuery(searchQueryseg);

			plStdStarSegment = new Block<>();
//			i = 0;
			for (Record searchRec : records) {
				plStdStarSegment.add(app.mapResultSetToClass(searchRec, PlStdStarSegment.class));
//				system.setCursorRecordIndex(i);
//				i++;
			}
			callPostquery("PL_STD_STAR_SEGMENT");
			plStdStarSegment.setLastQuery(hashUtils.encrypt(searchQueryseg));

			OracleHelpers.ResponseMapper(this, resDto);

			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto, total));
		} catch (Exception e) {
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> searchPlTldStar(
			AirportStarTriggerRequestDto reqDto, int page, int rec) throws Exception {
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			commonOncleardetail();

			PlTldStarQuerySearchDto pltldstarQuerySearch = new PlTldStarQuerySearchDto();
//			OracleHelpers.setMappingValues(plTldStar, pltldstarQuerySearch);
			OracleHelpers.bulkClassMapper(plTldStar, pltldstarQuerySearch);

//			OracleHelpers.bulkClassMapperV2(plTldStar, pltldstarQuerySearch, true);
			PlTldStarLegQuerySearchDto pltldstarlegQuerySearch = new PlTldStarLegQuerySearchDto();
			PlTldStarSegmentQuerySearchDto pltldstarsegmentQuerySearch = new PlTldStarSegmentQuerySearchDto();
			{
				// prequery block
				if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
					if (Objects.equals(toString(nameIn(this, "PL_TLD_STAR.LAST_WHERE")), null))
						pltldstarQuerySearch.setDataSupplier(global.getDataSupplier());
					if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
						pltldstarQuerySearch.setProcessingCycle(global.getProcessingCycle());

					}

				}
			}

			Long total = 0L;
			String searchQuerymas = "";
			List<Record> records = null;
			String sanitizedlastWhere = "";
			String lastWhere = toString(
					nameIn(nameIn(this, HoneyWellUtils.toCamelCase(system.getCursorBlock())), "lastWhere"));
			sanitizedlastWhere = app.sanitizeValueCheck(lastWhere);
//		      Field _queryField = OracleHelpers.getField(pltldstarQuerySearch,"lastWhere");
//		      if(_queryField!=null) {
//		    	  _queryField.setAccessible(true);
//					Object value = _queryField.get(pltldstarQuerySearch);
////					sanitizedqueryWhere = (String) value;
//					sanitizedlastWhere = app.sanitizeValueCheck((String) value);
//		      }
			String where = hashUtils.decrypt(global.getCallFormWhere());
			if (where != null)
				pltldstarQuerySearch = new PlTldStarQuerySearchDto();
			if (OracleHelpers.isNullorEmpty(sanitizedlastWhere)) {
				// Total Count Process
				String countQuery = app.getQuery(pltldstarQuerySearch, "pl_tld_star", where,
						"customer_ident,airport_ident,airport_icao,star_ident,processing_cycle", true, false);

				Record record = app.selectInto(countQuery);
				total = record.getLong();
				searchQuerymas = app.getQuery(pltldstarQuerySearch, "pl_tld_star", where,
						"customer_ident,airport_ident,airport_icao,star_ident,processing_cycle", false, false);
				;

				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuerymas, offset, rec);

				for (Record searchRec : records) {
					system.setCursorRecordIndex(0);
					this.plTldStar = app.mapResultSetToClass(searchRec, PlTldStar.class);
					callPostquery("PL_TLD_STAR");

				}
				plTldStar.setLastQuery(hashUtils.encrypt(searchQuerymas));
			} else {
				String countQuery = app.getQuery(pltldstarQuerySearch, "pl_tld_star", sanitizedlastWhere,
						"customer_ident,airport_ident,airport_icao,star_ident,processing_cycle", true, false);

				Record record = app.selectInto(app.sanitizeValueCheck(countQuery));
				total = record.getLong();
				searchQuerymas = app.getQuery(pltldstarQuerySearch, "pl_tld_star", sanitizedlastWhere,
						"customer_ident,airport_ident,airport_icao,star_ident,processing_cycle", false, false);

				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuerymas, offset, rec);

				for (Record searchRec : records) {
					system.setCursorRecordIndex(0);
					this.plTldStar = app.mapResultSetToClass(searchRec, PlTldStar.class);
					callPostquery("PL_TLD_STAR");

				}
				plTldStar.setLastQuery(hashUtils.encrypt(searchQuerymas));

			}

			// setting relation
			{

				pltldstarlegQuerySearch.setAirportIdent(plTldStar.getAirportIdent());
				pltldstarlegQuerySearch.setAirportIcao(plTldStar.getAirportIcao());
				pltldstarlegQuerySearch.setStarIdent(plTldStar.getStarIdent());
				pltldstarlegQuerySearch.setDataSupplier(plTldStar.getDataSupplier());
				pltldstarlegQuerySearch.setProcessingCycle(OracleHelpers.toString(plTldStar.getProcessingCycle()));
				pltldstarlegQuerySearch.setCustomerIdent(OracleHelpers.toString(plTldStar.getCustomerIdent()));

			}
			// countQuery = app.getQuery(pltldstarlegQuerySearch, "pl_tld_star_leg", "",
			// "route_type, transition_ident, sequence_num", true,true);
//			app.getQuery(pltldstarlegQuerySearch, "pl_tld_star_leg", "",
//					"route_type, transition_ident, sequence_num", true, true);
			// record = app.selectInto(countQuery);
			// total = record.getLong();
			String searchQueryleg = app.getQuery(pltldstarlegQuerySearch, "pl_tld_star_leg", "",
					"route_type, transition_ident, sequence_num", false, true);
			records = null;

			records = app.executeQuery(searchQueryleg);
			;

			plTldStarLeg = new Block<PlTldStarLeg>();
//			int i = 0;
			for (Record searchRec : records) {
				plTldStarLeg.add(app.mapResultSetToClass(searchRec, PlTldStarLeg.class));
//				system.setCursorRecordIndex(i);
//				i++;
			}
			callPostquery("PL_TLD_STAR_LEG");
			plTldStarLeg.setLastQuery(hashUtils.encrypt(searchQueryleg));

			{

				pltldstarsegmentQuerySearch.setAirportIdent(plTldStar.getAirportIdent());
				pltldstarsegmentQuerySearch.setAirportIcao(plTldStar.getAirportIcao());
				pltldstarsegmentQuerySearch.setStarIdent(plTldStar.getStarIdent());
				pltldstarsegmentQuerySearch.setDataSupplier(plTldStar.getDataSupplier());
				pltldstarsegmentQuerySearch.setProcessingCycle(OracleHelpers.toString(plTldStar.getProcessingCycle()));
				pltldstarsegmentQuerySearch.setCustomerIdent(OracleHelpers.toString(plTldStar.getCustomerIdent()));

			}

			// countQuery = app.getQuery(pltldstarsegmentQuerySearch, "pl_tld_star_segment",
			// "",
			// "route_type, transition_ident", true, true );
			app.getQuery(pltldstarsegmentQuerySearch, "pl_tld_star_segment", "", "route_type, transition_ident", true,
					true);
			// record = app.selectInto(countQuery);
			// total = record.getLong();
			String searchQueryseg = app.getQuery(pltldstarsegmentQuerySearch, "pl_tld_star_segment", "",
					"route_type, transition_ident", false, true);
			records = null;

			records = app.executeQuery(searchQueryseg);

			plTldStarSegment = new Block<>();
//			i = 0;
			for (Record searchRec : records) {
				plTldStarSegment.add(app.mapResultSetToClass(searchRec, PlTldStarSegment.class));
//				system.setCursorRecordIndex(i);
//				i++;
			}
			callPostquery("PL_TLD_STAR_SEGMENT");
			plTldStarSegment.setLastQuery(hashUtils.encrypt(searchQueryseg));
			OracleHelpers.ResponseMapper(this, resDto);

			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto, total));
		} catch (Exception e) {
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> toolsAddQueryConditions(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
			coreptMenuMmbServiceImpl.addQueryConditions(this);
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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> toolsDuplicate(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
			coreptMenuMmbServiceImpl.toolsDuplicate(this);
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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> toolsExportDestination(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		log.info(" whenValidateRecord Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
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
			BlockDetail childBlockData = null;

			// String Builders
			StringBuilder reportfile = new StringBuilder();
			List<Record> recs = null;
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
			if ("plStdStar".equals(HoneyWellUtils.toCamelCase(system.getCursorBlock()))
					|| "plStdStarLeg".equals(HoneyWellUtils.toCamelCase(system.getCursorBlock()))) {
				// Master Block
				mstBlockData = reqDto.getExportDataBlocks().get("plStdStar");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				} else if (query.contains(" offset ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " offset "));
				}

				// Child Block
				childBlockData = reqDto.getExportDataBlocks().get("plStdStarLeg");
				List<String> childPromptNames = getBlockMetaData(childBlockData, "PROMPT_NAME");
				List<String> childDatabseColumns = getBlockMetaData(childBlockData, "DATABASE_COLUMN");
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				reportfile.append(getExportHeader(childPromptNames, 1, selectOptions.getDelimiter()));

				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					PlStdStar plStdStar = app.mapResultSetToClass(mstRec, PlStdStar.class);
					// Post Query For Master
					this.plStdStar = plStdStar;
					// PlStdApproachPostQuery();
					reportfile.append(getExportData(this.plStdStar, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));

					// Fetching the Detail Blocks
					String childQuery = """
							Select * From PL_STD_STAR_LEG
							    where
							     ?   = AIRPORT_IDENT and
							    ?     = AIRPORT_ICAO and
							    ?   = STAR_IDENT and
							   ?    = DATA_SUPPLIER and
							    ? = PROCESSING_CYCLE
							  order by route_type, transition_ident, sequence_num
							 """;
					List<Record> childRecs = app.executeQuery(childQuery, plStdStar.getAirportIdent(),
							plStdStar.getAirportIcao(), plStdStar.getStarIdent(), plStdStar.getDataSupplier(),
							plStdStar.getProcessingCycle());
					reportfile.append(getChildExportData(childRecs, childDatabseColumns, 1, "plStdStarLeg"));
				}
			} else if ("plTldStar".equals(HoneyWellUtils.toCamelCase(system.getCursorBlock()))
					|| "plTldStarLeg".equals(HoneyWellUtils.toCamelCase(system.getCursorBlock()))) {
				// Master Block
				mstBlockData = reqDto.getExportDataBlocks().get("plTldStar");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				} else if (query.contains(" offset ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " offset "));
				}
				// Child Block
				childBlockData = reqDto.getExportDataBlocks().get("plTldStarLeg");
				List<String> childPromptNames = getBlockMetaData(childBlockData, "PROMPT_NAME");
				List<String> childDatabseColumns = getBlockMetaData(childBlockData, "DATABASE_COLUMN");
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				reportfile.append(getExportHeader(childPromptNames, 1, selectOptions.getDelimiter()));

				// Master Fetching..
				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					PlTldStar plTldStar = app.mapResultSetToClass(mstRec, PlTldStar.class);
					// Post Query For Master
					this.plTldStar = plTldStar;
					// PlTldApproachPostQuery();
					reportfile.append(getExportData(this.plTldStar, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));

					// Fetching the Detail Blocks
					String childQuery = """
							Select * From PL_TLD_STAR_LEG
							    where
							    ?   = AIRPORT_IDENT and
							    ?     = AIRPORT_ICAO and
							    ?   = STAR_IDENT and
							   ?    = DATA_SUPPLIER and
							    ? = PROCESSING_CYCLE and
							    ? = CUSTOMER_IDENT
							  order by route_type, transition_ident, sequence_num
							 """;
					List<Record> childRecs = app.executeQuery(childQuery, plTldStar.getAirportIdent(),
							plTldStar.getAirportIcao(), plTldStar.getStarIdent(), plTldStar.getDataSupplier(),
							plTldStar.getProcessingCycle(), plTldStar.getCustomerIdent());
					reportfile.append(getChildExportData(childRecs, childDatabseColumns, 1, "plTldStarLeg"));
				}
			} else if ("stdStar".equals(HoneyWellUtils.toCamelCase(system.getCursorBlock()))) {
				// Master Block
				mstBlockData = reqDto.getExportDataBlocks().get("stdStar");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				} else if (query.contains(" offset ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " offset "));
				}
				// Child Block
				childBlockData = reqDto.getExportDataBlocks().get("stdStarLeg");
				List<String> childPromptNames = getBlockMetaData(childBlockData, "PROMPT_NAME");
				List<String> childDatabseColumns = getBlockMetaData(childBlockData, "DATABASE_COLUMN");
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				reportfile.append(getExportHeader(childPromptNames, 1, selectOptions.getDelimiter()));

				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					StdStar stdStar = app.mapResultSetToClass(mstRec, StdStar.class);
					reportfile.append(getExportData(stdStar, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));

					// Fetching the Detail Blocks
					String childQuery = """
							Select * From STD_STAR_LEG
							  where
							    ?   = AIRPORT_IDENT and
							    ?     = AIRPORT_ICAO and
							    ?   = STAR_IDENT and
							   ?    = DATA_SUPPLIER
							  order by route_type, transition_ident, sequence_num
							 """;
					List<Record> childRecs = app.executeQuery(childQuery, stdStar.getAirportIdent(),
							stdStar.getAirportIcao(), stdStar.getStarIdent(), stdStar.getDataSupplier());
					reportfile.append(getChildExportData(childRecs, childDatabseColumns, 1, "stdStarLeg"));
				}
			}

			else if ("tldStar".equals(HoneyWellUtils.toCamelCase(system.getCursorBlock()))) {
				// Master Block
				mstBlockData = reqDto.getExportDataBlocks().get("tldStar");
				List<String> mstPromptNames = getBlockMetaData(mstBlockData, "PROMPT_NAME");
				List<String> mstDatabseColumns = getBlockMetaData(mstBlockData, "DATABASE_COLUMN");
				String query = hashUtils.decrypt(mstBlockData.getLastQuery());
				if (query.contains(" where rno <=")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " where rno <="));
				} else if (query.contains(" OFFSET ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " OFFSET "));
				} else if (query.contains(" offset ")) {
					query = substrWithLen(query, 0, OracleHelpers.instr(query, " offset "));
				}
				// Child Block
				childBlockData = reqDto.getExportDataBlocks().get("tldStarLeg");
				List<String> childPromptNames = getBlockMetaData(childBlockData, "PROMPT_NAME");
				List<String> childDatabseColumns = getBlockMetaData(childBlockData, "DATABASE_COLUMN");
				// Header Building..
				reportfile.append(getExportHeader(mstPromptNames, 0, selectOptions.getDelimiter()));
				reportfile.append(getExportHeader(childPromptNames, 1, selectOptions.getDelimiter()));

				recs = app.executeQuery(query);
				for (Record mstRec : recs) {
					TldStar tldStar = app.mapResultSetToClass(mstRec, TldStar.class);
					reportfile.append(getExportData(tldStar, mstDatabseColumns, 0, selectOptions.getDelimiter(),
							selectOptions.getGetTextFile()));

					// Fetching the Detail Blocks
					String childQuery = """
							Select * From TLD_STAR_LEG
							    where  ?   = AIRPORT_IDENT and
							    ?     = AIRPORT_ICAO and
							    ?   = STAR_IDENT and
							   ?    = DATA_SUPPLIER and
							    ? = CUSTOMER_IDENT
							  order by route_type, transition_ident, sequence_num
							 """;
					List<Record> childRecs = app.executeQuery(childQuery, tldStar.getAirportIdent(),
							tldStar.getAirportIcao(), tldStar.getStarIdent(), tldStar.getDataSupplier(),
							tldStar.getCustomerIdent());
					reportfile.append(getChildExportData(childRecs, childDatabseColumns, 1, "tldStarLeg"));
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
		} catch (Exception e) {
			log.error("Error while Executing the displayItemBlockRefreshButtonWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}

	}

	// Child Data Extraction
	public String getChildExportData(List<Record> recs, List<String> columns, int depth, String childBlockName)
			throws Exception {
		StringBuilder data = new StringBuilder();
		if (recs.size() <= 0) {
			if ("plStdStarLeg".equals(childBlockName)) {
				PlStdStarLeg plStdStarLeg = new PlStdStarLeg();
				data.append(getExportData(plStdStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			} else if ("plTldStarLeg".equals(childBlockName)) {
				PlTldStarLeg plTldStarLeg = new PlTldStarLeg();
				data.append(getExportData(plTldStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}

			else if ("stdStarLeg".equals(childBlockName)) {
				StdStarLeg stdStarLeg = new StdStarLeg();
				data.append(getExportData(stdStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}

			else if ("tldStarLeg".equals(childBlockName)) {
				TldStarLeg tldStarLeg = new TldStarLeg();
				data.append(getExportData(tldStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}

		}
		// Reset Blocks
		this.plStdStarLeg.getData().clear();
		this.plTldStarLeg.getData().clear();
		this.tldStarLeg.getData().clear();
		this.stdStarLeg.getData().clear();
		for (Record rec : recs) {
			if ("plStdStarLeg".equals(childBlockName)) {
				PlStdStarLeg plStdStarLeg = app.mapResultSetToClass(rec, PlStdStarLeg.class);
				this.plStdStarLeg.add(plStdStarLeg);
			}

			else if ("plTldStarLeg".equals(childBlockName)) {
				PlTldStarLeg plTldStarLeg = app.mapResultSetToClass(rec, PlTldStarLeg.class);
				this.plTldStarLeg.add(plTldStarLeg);
			}

			else if ("stdStarLeg".equals(childBlockName)) {
				StdStarLeg stdStarLeg = app.mapResultSetToClass(rec, StdStarLeg.class);
				this.stdStarLeg.add(stdStarLeg);
			}

			else if ("tldStarLeg".equals(childBlockName)) {
				TldStarLeg tldStarLeg = app.mapResultSetToClass(rec, TldStarLeg.class);
				this.tldStarLeg.add(tldStarLeg);
			}

		}
		if ("plStdStarLeg".equals(childBlockName)) {
			// Post Query for Child
			PlStdStarLegPostQuery();
			for (PlStdStarLeg plStdStarLeg : this.plStdStarLeg.getData()) {
				data.append(getExportData(plStdStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}
		}

		else if ("plTldStarLeg".equals(childBlockName)) {
			// Post Query for Child
			PlTldStarLegPostQuery();
			for (PlTldStarLeg plTldStarLeg : this.plTldStarLeg.getData()) {
				data.append(getExportData(plTldStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}
		}

		else if ("stdStarLeg".equals(childBlockName)) {
			// Post Query for Child
			stdStarLegPostQuery();
			for (StdStarLeg stdStarLeg : this.stdStarLeg.getData()) {
				data.append(getExportData(stdStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}
		}

		else if ("tldStarLeg".equals(childBlockName)) {
			// Post Query for Child
			tldStarLegPostQuery();
			for (TldStarLeg tldStarLeg : this.tldStarLeg.getData()) {
				data.append(getExportData(tldStarLeg, columns, depth, selectOptions.getDelimiter(),
						selectOptions.getGetTextFile()));
			}
		}

		return data.toString();

	}

	private void PlTldStarLegPostQuery() {
		try {
			for (PlTldStarLeg leg : plTldStarLeg.getData()) {
				if (!Objects.equals(leg.getWaypointDescCode(), null)) {
					leg.setWc1(substr(rpad(leg.getWaypointDescCode(), 4), 1, 1).trim());
					leg.setWc2(substr(rpad(leg.getWaypointDescCode(), 4), 2, 1).trim());
					leg.setWc3(substr(rpad(leg.getWaypointDescCode(), 4), 3, 1).trim());
					leg.setWc4(substr(rpad(leg.getWaypointDescCode(), 4), 4, 1).trim());

				}
			}

			// mergeDelete();
			log.info(" plStdStarLegPostQuery executed successfully");

		} catch (Exception e) {
			throw e;
		}
	}

	private void PlStdStarLegPostQuery() {
		try {
			for (PlStdStarLeg leg : plStdStarLeg.getData()) {
				if (!Objects.equals(leg.getWaypointDescCode(), null)) {
					leg.setWc1(substr(rpad(leg.getWaypointDescCode(), 4), 1, 1).trim());
					leg.setWc2(substr(rpad(leg.getWaypointDescCode(), 4), 2, 1).trim());
					leg.setWc3(substr(rpad(leg.getWaypointDescCode(), 4), 3, 1).trim());
					leg.setWc4(substr(rpad(leg.getWaypointDescCode(), 4), 4, 1).trim());

				}
			}

			// mergeDelete();
			log.info(" plStdStarLegPostQuery executed successfully");

		} catch (Exception e) {
			throw e;
		}
	}

	public void mergeDelete() {
		if (plTldStarSegment != null && plTldStarSegment.getDeletedRecords().size() > 0) {
			plTldStarSegment.getData().addAll(plTldStarSegment.getDeletedRecords());
		}
		if (plTldStarLeg != null && plTldStarLeg.getDeletedRecords().size() > 0) {
			plTldStarLeg.getData().addAll(plTldStarLeg.getDeletedRecords());
		}
		if (plStdStarSegment != null && plStdStarSegment.getDeletedRecords().size() > 0) {
			plStdStarSegment.getData().addAll(plStdStarSegment.getDeletedRecords());
		}
		if (plStdStarLeg != null && plStdStarLeg.getDeletedRecords().size() > 0) {
			plStdStarLeg.getData().addAll(plStdStarLeg.getDeletedRecords());
		}
	}

	private void commonOncleardetail() throws Exception {
		try {
			if (Objects.equals(system.getFormStatus(), "CHANGED")
					&& !Arrays.asList("DELETE_RECORD", "COUNT_QUERY", "DOWN", "NEXT_RECORD")
							.contains(system.getCoordinationOperation())) {

				// TODO CHECK_TO_COMMIT(:system.COORDINATION_OPERATION) --- Program Unit Calling
				mergeDelete();
				checkToCommit(system.getCoordinationOperation());
				if (Objects.equals(system.getFormStatus(), "CHANGED")) {
					throw new FormTriggerFailureException(event);

				}

			}

			// TODO Clear_All_Master_Details --- Program Unit Calling
			// this.plTldStar = new PlTldStar();
			// this.plStdStar = new PlStdStar();
			this.plTldStarLeg = new Block<>();
			this.plTldStarSegment = new Block<>();
			this.plStdStarLeg = new Block<>();
			this.plStdStarSegment = new Block<>();
			if (Objects.equals(parameter.getRecordType(), "S")) {
				setItemProperty("control_block.std_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_FALSE);
				setItemProperty("control_block.std_leg_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);
				controlBlock.setStdOverrideErrors(null);
				setItemProperty("control_block.std_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

			}

			else {
				setItemProperty("control_block.tld_validation_errors", FormConstant.VISIBLE,
						FormConstant.PROPERTY_FALSE);
				setItemProperty("control_block.tld_leg_errors", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);
				controlBlock.setTldOverrideErrors(null);
				setItemProperty("control_block.tld_overide", FormConstant.VISIBLE, FormConstant.PROPERTY_FALSE);

			}
		} catch (Exception ex) {
			throw ex;
		}

	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> onError(AirportStarTriggerRequestDto reqDto)
			throws Exception {
		log.info(" onError Executing");
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapperV2(reqDto, this, true);
			Integer msgnum = 0;
			String msgtxt = null;
			String msgtyp = null;
			String vBlockName = system.getCursorBlock();

			if ((Objects.equals(msgnum, 40400) || Objects.equals(msgnum, 40406) || Objects.equals(msgnum, 40407))) {
				message("changes saved successfully");

			}

			else if (Arrays.asList(41051, 40350, 47316, 40353, 40352).contains(msgnum)) {
				;

			}

			else if (Objects.equals(msgnum, 41050) && Objects.equals(parameter.getWorkType(), "VIEW")) {
				;

			}

			else if (Arrays.asList(40401, 40405).contains(msgnum)) {
				;

			}

			else if (Objects.equals(msgnum, 40100)) {

				message("at the first record.");

			}

			else if (Objects.equals(msgnum, 40735) && like("%01031%", msgtxt)) {

				clearMessage();

				coreptLib.dspMsg(msgtxt + " Insufficient privileges. ");
			}

			else if (Objects.equals(global.getErrorCode(), 40501)) {
				controlBlock.setTempField("Y");

			}

			else if (Arrays.asList(40508, 40509).contains(global.getErrorCode())) {

				coreptLib.dspMsg(msgtxt + chr(10) + chr(10)
						+ "Please check the exact error message from the \"Display Error\" in the \"HELP\" menu");

			}

			else if (Arrays.asList(40200).contains(global.getErrorCode())) {
				if (Objects.equals(parameter.getUpdRec(), "N")) {
					if (!Objects.equals(parameter.getWorkType(), "VIEW")) {
						if (Objects.equals(parameter.getRecordType(), "T")) {
							if (Objects.equals(nameIn(this, vBlockName + ".processing_cycle"), null)) {

								coreptLib.dspMsg("Record Cannot be Updated as "
										+ nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")
										+ " is not associated \n with DCR# " + global.getDcrNumber()
										+ " Or with processing Cycle " + global.getProcessingCycle());

							}

							else {

								coreptLib.dspMsg("Record Cannot be Updated as "
										+ nameIn(this, system.getCursorBlock() + ".CUSTOMER_IDENT")
										+ " is not associated \n with DCR# " + global.getDcrNumber()
										+ " Or with processing Cycle "
										+ nameIn(this, vBlockName + ".processing_cycle"));

							}
							throw new FormTriggerFailureException();

						}

						else {
							if (Objects.equals(toInteger(nameIn(this, vBlockName + ".processing_cycle")), null)) {

								coreptLib.dspMsg("STD Record Cannot be Updated with DCR# " + global.getDcrNumber()
										+ " Or \n with processing Cycle " + global.getProcessingCycle());

							}

							else {

								coreptLib.dspMsg("STD Record Cannot be Updated with DCR# " + global.getDcrNumber()
										+ " Or \n with processing Cycle "
										+ toString(nameIn(this, vBlockName + ".processing_cycle")));

							}
							throw new FormTriggerFailureException();

						}

					}

					else {
						coreptLib.dspMsg(msgtxt);
						throw new FormTriggerFailureException();

					}

				}

				else {

					coreptLib.dspMsg(msgtxt);
					throw new FormTriggerFailureException();

				}

			}

			else if (Objects.equals(msgnum, 41050) && !Objects.equals(parameter.getWorkType(), "VIEW")) {
				if (Objects.equals(parameter.getUpdRec(), "N")) {
					;

				}

				else {

					coreptLib.dspMsg(msgtxt);
					throw new FormTriggerFailureException();

				}

			}

			else {
				displayAlert.oneButton("S", "Error", msgtyp + "-" + msgnum + ":" + msgtxt);

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
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> tldStarLegPostQuery(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();

		// TODO Auto-generated method stub
		try {
			OracleHelpers.bulkClassMapperV2(reqDto, this, true);

			for (TldStarLeg leg : tldStarLeg.getData()) {
				if (!Objects.equals(leg.getWaypointDescCode(), null)) {
					leg.setWc1(substr(rpad(leg.getWaypointDescCode(), 4), 1, 1).trim());
					leg.setWc2(substr(rpad(leg.getWaypointDescCode(), 4), 2, 1).trim());
					leg.setWc3(substr(rpad(leg.getWaypointDescCode(), 4), 3, 1).trim());
					leg.setWc4(substr(rpad(leg.getWaypointDescCode(), 4), 4, 1).trim());

				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarLegPostQuery executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStarLegPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> stdStarLegPostQuery(
			AirportStarTriggerRequestDto reqDto) throws Exception {
		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapperV2(reqDto, this, true);

			for (StdStarLeg leg : stdStarLeg.getData()) {
				if (!Objects.equals(leg.getWaypointDescCode(), null)) {
					leg.setWc1(substr(rpad(leg.getWaypointDescCode(), 4), 1, 1).trim());
					leg.setWc2(substr(rpad(leg.getWaypointDescCode(), 4), 2, 1).trim());
					leg.setWc3(substr(rpad(leg.getWaypointDescCode(), 4), 3, 1).trim());
					leg.setWc4(substr(rpad(leg.getWaypointDescCode(), 4), 4, 1).trim());

				}
			}

			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" tldStarLegPostQuery executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the tldStarLegPostQuery Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

//	@Override
//	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> plTldStarLegPostQuery(
//			AirportStarTriggerRequestDto reqDto) throws Exception {
//		log.info(" onError Executing");
//		BaseResponse<AirportStarTriggerResponseDto> responseObj = new BaseResponse<>();
//		AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
//		OracleHelpers.bulkClassMapperV2(reqDto, this, true);
//		// TODO Auto-generated method stub
//		try {
//			if (!Objects.equals(tldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), null)) {
//				tldStarLeg.getRow(system.getCursorRecordIndex()).setWc1(
//						substr(rpad(tldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), 4), 1, 1));
//				tldStarLeg.getRow(system.getCursorRecordIndex()).setWc2(
//						substr(rpad(tldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), 4), 2, 1));
//				tldStarLeg.getRow(system.getCursorRecordIndex()).setWc3(
//						substr(rpad(tldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), 4), 3, 1));
//				tldStarLeg.getRow(system.getCursorRecordIndex()).setWc4(
//						substr(rpad(tldStarLeg.getRow(system.getCursorRecordIndex()).getWaypointDescCode(), 4), 4, 1));
//
//			}
//
//			OracleHelpers.ResponseMapper(this, resDto);
//			log.info(" tldStarLegPostQuery executed successfully");
//			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
//		} catch (Exception e) {
//			log.error("Error while Executing the tldStarLegPostQuery Service");
//			OracleHelpers.ResponseMapper(this, resDto);
//			return ExceptionUtils.handleException(e, resDto);
//		}
//	}

}

------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarLegQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarLegRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IPlStdStarLegRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IPlStdStarLegService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlStdStarLeg Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class PlStdStarLegServiceImpl implements IPlStdStarLegService {

	@Autowired
	IPlStdStarLegRepository plstdstarlegRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of PlStdStarLeg with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of PlStdStarLeg based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarLeg>>> getAllPlStdStarLeg(int page, int rec) {
		BaseResponse<List<PlStdStarLeg>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all PlStdStarLeg Data");
			if (page == -1 && rec == -1) {
				List<PlStdStarLeg> plstdstarleg = plstdstarlegRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, plstdstarleg));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<PlStdStarLeg> plstdstarlegPages = plstdstarlegRepository.findAll(pages);
			if (plstdstarlegPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						plstdstarlegPages.getContent(), plstdstarlegPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all PlStdStarLeg data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific PlStdStarLeg data by its ID.
	 *
	 * @param id The ID of the PlStdStarLeg to retrieve.
	 * @return A ResponseDto containing the PlStdStarLeg entity with the specified
	 *         ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<PlStdStarLeg>> getPlStdStarLegById(Long id) {
		BaseResponse<PlStdStarLeg> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching PlStdStarLeg Data By Id");
			Optional<PlStdStarLeg> plstdstarleg = plstdstarlegRepository.findById(id);
			if (plstdstarleg.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, plstdstarleg.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching PlStdStarLeg data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Creates new PlStdStarLegs based on the provided list of DTOs.
	 *
	 * @param createplstdstarlegs The list of DTOs containing data for creating
	 *                            PlStdStarLeg.
	 * @return A ResponseDto containing the list of created PlStdStarLeg entities.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarLeg>>>
	// createPlStdStarLeg(List<PlStdStarLegRequestDto> plstdstarlegsCreate) {
	// BaseResponse<List<PlStdStarLeg>> responseObj = new BaseResponse<>();
	// List<PlStdStarLeg> createdPlStdStarLegs = new ArrayList<>();
	//
	// for (PlStdStarLegRequestDto plstdstarlegCreate : plstdstarlegsCreate) {
	// try {
	// log.info("Creating PlStdStarLeg Data");
	// PlStdStarLeg plstdstarleg = new PlStdStarLeg();
	// plstdstarleg.setRouteType(plstdstarlegCreate.getRouteType());
	// plstdstarleg.setTransitionIdent(plstdstarlegCreate.getTransitionIdent());
	// plstdstarleg.setAirportIdent(plstdstarlegCreate.getAirportIdent());
	// plstdstarleg.setSequenceNum(plstdstarlegCreate.getSequenceNum());
	// plstdstarleg.setAirportIcao(plstdstarlegCreate.getAirportIcao());
	// plstdstarleg.setProcessingCycle(plstdstarlegCreate.getProcessingCycle());
	// plstdstarleg.setDataSupplier(plstdstarlegCreate.getDataSupplier());
	// plstdstarleg.setWaypointDescCode(plstdstarlegCreate.getWaypointDescCode());
	// plstdstarleg.setTurnDirection(plstdstarlegCreate.getTurnDirection());
	// plstdstarleg.setRnp(plstdstarlegCreate.getRnp());
	// plstdstarleg.setPathAndTermination(plstdstarlegCreate.getPathAndTermination());
	// plstdstarleg.setTurnDirValid(plstdstarlegCreate.getTurnDirValid());
	// plstdstarleg.setArcRadius(plstdstarlegCreate.getArcRadius());
	// plstdstarleg.setTheta(plstdstarlegCreate.getTheta());
	// plstdstarleg.setRho(plstdstarlegCreate.getRho());
	// plstdstarleg.setMagneticCourse(plstdstarlegCreate.getMagneticCourse());
	// plstdstarleg.setRouteDistance(plstdstarlegCreate.getRouteDistance());
	// plstdstarleg.setAltDescription(plstdstarlegCreate.getAltDescription());
	// plstdstarleg.setAtcInd(plstdstarlegCreate.getAtcInd());
	// plstdstarleg.setAlt1(plstdstarlegCreate.getAlt1());
	// plstdstarleg.setAlt2(plstdstarlegCreate.getAlt2());
	// plstdstarleg.setTransAltitude(plstdstarlegCreate.getTransAltitude());
	// plstdstarleg.setFixIdent(plstdstarlegCreate.getFixIdent());
	// plstdstarleg.setSpeedLimit(plstdstarlegCreate.getSpeedLimit());
	// plstdstarleg.setFixIcaoCode(plstdstarlegCreate.getFixIcaoCode());
	// plstdstarleg.setVerticalAngle(plstdstarlegCreate.getVerticalAngle());
	// plstdstarleg.setCenterFixMultipleCode(plstdstarlegCreate.getCenterFixMultipleCode());
	// plstdstarleg.setFixSectionCode(plstdstarlegCreate.getFixSectionCode());
	// plstdstarleg.setFixSubsectionCode(plstdstarlegCreate.getFixSubsectionCode());
	// plstdstarleg.setRecommNavaidIdent(plstdstarlegCreate.getRecommNavaidIdent());
	// plstdstarleg.setRecommNavaidIcaoCode(plstdstarlegCreate.getRecommNavaidIcaoCode());
	// plstdstarleg.setRecommNavaidSection(plstdstarlegCreate.getRecommNavaidSection());
	// plstdstarleg.setRecommNavaidSubsection(plstdstarlegCreate.getRecommNavaidSubsection());
	// plstdstarleg.setCenterFixIdent(plstdstarlegCreate.getCenterFixIdent());
	// plstdstarleg.setCenterFixIcaoCode(plstdstarlegCreate.getCenterFixIcaoCode());
	// plstdstarleg.setCenterFixSection(plstdstarlegCreate.getCenterFixSection());
	// plstdstarleg.setCenterFixSubsection(plstdstarlegCreate.getCenterFixSubsection());
	// plstdstarleg.setFileRecno(plstdstarlegCreate.getFileRecno());
	// plstdstarleg.setCycleData(plstdstarlegCreate.getCycleData());
	// plstdstarleg.setRouteType(plstdstarlegCreate.getRouteType());
	// plstdstarleg.setCenterFixIdent(plstdstarlegCreate.getCenterFixIdent());
	// plstdstarleg.setProcDesignMagVar(plstdstarlegCreate.getProcDesignMagVar());
	// plstdstarleg.setMagneticCourse(plstdstarlegCreate.getMagneticCourse());
	// plstdstarleg.setCenterFixMultipleCode(plstdstarlegCreate.getCenterFixMultipleCode());
	// plstdstarleg.setAirportIdent(plstdstarlegCreate.getAirportIdent());
	// plstdstarleg.setRnp(plstdstarlegCreate.getRnp());
	// plstdstarleg.setFixSubsectionCode(plstdstarlegCreate.getFixSubsectionCode());
	// plstdstarleg.setCenterFixIcaoCode(plstdstarlegCreate.getCenterFixIcaoCode());
	// plstdstarleg.setRecommNavaidSection(plstdstarlegCreate.getRecommNavaidSection());
	// plstdstarleg.setAltDescription(plstdstarlegCreate.getAltDescription());
	// plstdstarleg.setSpeedLimitDesc(plstdstarlegCreate.getSpeedLimitDesc());
	// plstdstarleg.setArcRadius(plstdstarlegCreate.getArcRadius());
	// plstdstarleg.setValidateInd(plstdstarlegCreate.getValidateInd());
	// plstdstarleg.setUpdateDcrNumber(plstdstarlegCreate.getUpdateDcrNumber());
	// plstdstarleg.setTransAltitude(plstdstarlegCreate.getTransAltitude());
	// plstdstarleg.setSequenceNum(plstdstarlegCreate.getSequenceNum());
	// plstdstarleg.setTheta(plstdstarlegCreate.getTheta());
	// plstdstarleg.setCreateDcrNumber(plstdstarlegCreate.getCreateDcrNumber());
	// plstdstarleg.setFixSectionCode(plstdstarlegCreate.getFixSectionCode());
	// plstdstarleg.setDataSupplier(plstdstarlegCreate.getDataSupplier());
	// plstdstarleg.setRouteDistance(plstdstarlegCreate.getRouteDistance());
	// plstdstarleg.setRecommNavaidIdent(plstdstarlegCreate.getRecommNavaidIdent());
	// plstdstarleg.setRecommNavaidSubsection(plstdstarlegCreate.getRecommNavaidSubsection());
	// plstdstarleg.setRecommNavaidIcaoCode(plstdstarlegCreate.getRecommNavaidIcaoCode());
	// plstdstarleg.setCenterFixSubsection(plstdstarlegCreate.getCenterFixSubsection());
	// plstdstarleg.setTurnDirection(plstdstarlegCreate.getTurnDirection());
	// plstdstarleg.setAlt2(plstdstarlegCreate.getAlt2());
	// plstdstarleg.setAlt1(plstdstarlegCreate.getAlt1());
	// plstdstarleg.setCenterFixSection(plstdstarlegCreate.getCenterFixSection());
	// plstdstarleg.setFixIcaoCode(plstdstarlegCreate.getFixIcaoCode());
	// plstdstarleg.setSpeedLimit(plstdstarlegCreate.getSpeedLimit());
	// plstdstarleg.setStarIdent(plstdstarlegCreate.getStarIdent());
	// plstdstarleg.setVerticalAngle(plstdstarlegCreate.getVerticalAngle());
	// plstdstarleg.setPathAndTermination(plstdstarlegCreate.getPathAndTermination());
	// plstdstarleg.setTransitionIdent(plstdstarlegCreate.getTransitionIdent());
	// plstdstarleg.setFixIdent(plstdstarlegCreate.getFixIdent());
	// plstdstarleg.setRho(plstdstarlegCreate.getRho());
	// plstdstarleg.setAtcInd(plstdstarlegCreate.getAtcInd());
	// plstdstarleg.setCycleData(plstdstarlegCreate.getCycleData());
	// plstdstarleg.setFileRecno(plstdstarlegCreate.getFileRecno());
	// plstdstarleg.setAircraftType(plstdstarlegCreate.getAircraftType());
	// plstdstarleg.setWaypointDescCode(plstdstarlegCreate.getWaypointDescCode());
	// plstdstarleg.setAirportIcao(plstdstarlegCreate.getAirportIcao());
	// plstdstarleg.setProcessingCycle(plstdstarlegCreate.getProcessingCycle());
	// plstdstarleg.setTurnDirValid(plstdstarlegCreate.getTurnDirValid());
	// PlStdStarLeg createdPlStdStarLeg = plstdstarlegRepository.save(plstdstarleg);
	// createdPlStdStarLegs.add(createdPlStdStarLeg);
	// } catch (Exception ex) {
	// log.error("An error occurred while creating PlStdStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED,
	// createdPlStdStarLegs));
	// }
	//
	// /**
	// * Updates existing PlStdStarLegs based on the provided list of DTOs.
	// *
	// * @param plstdstarlegsUpdate The list of DTOs containing data for updating
	// PlStdStarLeg.
	// * @return A ResponseDto containing the list of updated PlStdStarLeg entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarLeg>>>
	// updatePlStdStarLeg(List<PlStdStarLegRequestDto> plstdstarlegsUpdate) {
	// BaseResponse<List<PlStdStarLeg>> responseObj = new BaseResponse<>();
	// List<PlStdStarLeg> updatedPlStdStarLegs = new ArrayList<>();
	//
	// for (PlStdStarLegRequestDto plstdstarlegUpdate : plstdstarlegsUpdate) {
	// try {
	// log.info("Updating PlStdStarLeg Data");
	// PlStdStarLegIdClass PlStdStarLegId = new
	// PlStdStarLegIdClass(plstdstarlegUpdate.getAircraftType(),plstdstarlegUpdate.getAirportIcao(),plstdstarlegUpdate.getTransitionIdent(),plstdstarlegUpdate.getRouteType(),plstdstarlegUpdate.getAirportIdent(),plstdstarlegUpdate.getSequenceNum(),plstdstarlegUpdate.getStarIdent(),plstdstarlegUpdate.getDataSupplier(),plstdstarlegUpdate.getProcessingCycle());
	// Optional<PlStdStarLeg> existingPlStdStarLegOptional =
	// plstdstarlegRepository.findById(PlStdStarLegId);
	// if (existingPlStdStarLegOptional.isPresent()) {
	// PlStdStarLeg existingPlStdStarLeg = existingPlStdStarLegOptional.get();
	// existingPlStdStarLeg.setRouteType(plstdstarlegUpdate.getRouteType());
	// existingPlStdStarLeg.setTransitionIdent(plstdstarlegUpdate.getTransitionIdent());
	// existingPlStdStarLeg.setAirportIdent(plstdstarlegUpdate.getAirportIdent());
	// existingPlStdStarLeg.setSequenceNum(plstdstarlegUpdate.getSequenceNum());
	// existingPlStdStarLeg.setAirportIcao(plstdstarlegUpdate.getAirportIcao());
	// existingPlStdStarLeg.setProcessingCycle(plstdstarlegUpdate.getProcessingCycle());
	// existingPlStdStarLeg.setDataSupplier(plstdstarlegUpdate.getDataSupplier());
	// existingPlStdStarLeg.setWaypointDescCode(plstdstarlegUpdate.getWaypointDescCode());
	// existingPlStdStarLeg.setTurnDirection(plstdstarlegUpdate.getTurnDirection());
	// existingPlStdStarLeg.setRnp(plstdstarlegUpdate.getRnp());
	// existingPlStdStarLeg.setPathAndTermination(plstdstarlegUpdate.getPathAndTermination());
	// existingPlStdStarLeg.setTurnDirValid(plstdstarlegUpdate.getTurnDirValid());
	// existingPlStdStarLeg.setArcRadius(plstdstarlegUpdate.getArcRadius());
	// existingPlStdStarLeg.setTheta(plstdstarlegUpdate.getTheta());
	// existingPlStdStarLeg.setRho(plstdstarlegUpdate.getRho());
	// existingPlStdStarLeg.setMagneticCourse(plstdstarlegUpdate.getMagneticCourse());
	// existingPlStdStarLeg.setRouteDistance(plstdstarlegUpdate.getRouteDistance());
	// existingPlStdStarLeg.setAltDescription(plstdstarlegUpdate.getAltDescription());
	// existingPlStdStarLeg.setAtcInd(plstdstarlegUpdate.getAtcInd());
	// existingPlStdStarLeg.setAlt1(plstdstarlegUpdate.getAlt1());
	// existingPlStdStarLeg.setAlt2(plstdstarlegUpdate.getAlt2());
	// existingPlStdStarLeg.setTransAltitude(plstdstarlegUpdate.getTransAltitude());
	// existingPlStdStarLeg.setFixIdent(plstdstarlegUpdate.getFixIdent());
	// existingPlStdStarLeg.setSpeedLimit(plstdstarlegUpdate.getSpeedLimit());
	// existingPlStdStarLeg.setFixIcaoCode(plstdstarlegUpdate.getFixIcaoCode());
	// existingPlStdStarLeg.setVerticalAngle(plstdstarlegUpdate.getVerticalAngle());
	// existingPlStdStarLeg.setCenterFixMultipleCode(plstdstarlegUpdate.getCenterFixMultipleCode());
	// existingPlStdStarLeg.setFixSectionCode(plstdstarlegUpdate.getFixSectionCode());
	// existingPlStdStarLeg.setFixSubsectionCode(plstdstarlegUpdate.getFixSubsectionCode());
	// existingPlStdStarLeg.setRecommNavaidIdent(plstdstarlegUpdate.getRecommNavaidIdent());
	// existingPlStdStarLeg.setRecommNavaidIcaoCode(plstdstarlegUpdate.getRecommNavaidIcaoCode());
	// existingPlStdStarLeg.setRecommNavaidSection(plstdstarlegUpdate.getRecommNavaidSection());
	// existingPlStdStarLeg.setRecommNavaidSubsection(plstdstarlegUpdate.getRecommNavaidSubsection());
	// existingPlStdStarLeg.setCenterFixIdent(plstdstarlegUpdate.getCenterFixIdent());
	// existingPlStdStarLeg.setCenterFixIcaoCode(plstdstarlegUpdate.getCenterFixIcaoCode());
	// existingPlStdStarLeg.setCenterFixSection(plstdstarlegUpdate.getCenterFixSection());
	// existingPlStdStarLeg.setCenterFixSubsection(plstdstarlegUpdate.getCenterFixSubsection());
	// existingPlStdStarLeg.setFileRecno(plstdstarlegUpdate.getFileRecno());
	// existingPlStdStarLeg.setCycleData(plstdstarlegUpdate.getCycleData());
	// existingPlStdStarLeg.setRouteType(plstdstarlegUpdate.getRouteType());
	// existingPlStdStarLeg.setCenterFixIdent(plstdstarlegUpdate.getCenterFixIdent());
	// existingPlStdStarLeg.setProcDesignMagVar(plstdstarlegUpdate.getProcDesignMagVar());
	// existingPlStdStarLeg.setMagneticCourse(plstdstarlegUpdate.getMagneticCourse());
	// existingPlStdStarLeg.setCenterFixMultipleCode(plstdstarlegUpdate.getCenterFixMultipleCode());
	// existingPlStdStarLeg.setAirportIdent(plstdstarlegUpdate.getAirportIdent());
	// existingPlStdStarLeg.setRnp(plstdstarlegUpdate.getRnp());
	// existingPlStdStarLeg.setFixSubsectionCode(plstdstarlegUpdate.getFixSubsectionCode());
	// existingPlStdStarLeg.setCenterFixIcaoCode(plstdstarlegUpdate.getCenterFixIcaoCode());
	// existingPlStdStarLeg.setRecommNavaidSection(plstdstarlegUpdate.getRecommNavaidSection());
	// existingPlStdStarLeg.setAltDescription(plstdstarlegUpdate.getAltDescription());
	// existingPlStdStarLeg.setSpeedLimitDesc(plstdstarlegUpdate.getSpeedLimitDesc());
	// existingPlStdStarLeg.setArcRadius(plstdstarlegUpdate.getArcRadius());
	// existingPlStdStarLeg.setValidateInd(plstdstarlegUpdate.getValidateInd());
	// existingPlStdStarLeg.setUpdateDcrNumber(plstdstarlegUpdate.getUpdateDcrNumber());
	// existingPlStdStarLeg.setTransAltitude(plstdstarlegUpdate.getTransAltitude());
	// existingPlStdStarLeg.setSequenceNum(plstdstarlegUpdate.getSequenceNum());
	// existingPlStdStarLeg.setTheta(plstdstarlegUpdate.getTheta());
	// existingPlStdStarLeg.setCreateDcrNumber(plstdstarlegUpdate.getCreateDcrNumber());
	// existingPlStdStarLeg.setFixSectionCode(plstdstarlegUpdate.getFixSectionCode());
	// existingPlStdStarLeg.setDataSupplier(plstdstarlegUpdate.getDataSupplier());
	// existingPlStdStarLeg.setRouteDistance(plstdstarlegUpdate.getRouteDistance());
	// existingPlStdStarLeg.setRecommNavaidIdent(plstdstarlegUpdate.getRecommNavaidIdent());
	// existingPlStdStarLeg.setRecommNavaidSubsection(plstdstarlegUpdate.getRecommNavaidSubsection());
	// existingPlStdStarLeg.setRecommNavaidIcaoCode(plstdstarlegUpdate.getRecommNavaidIcaoCode());
	// existingPlStdStarLeg.setCenterFixSubsection(plstdstarlegUpdate.getCenterFixSubsection());
	// existingPlStdStarLeg.setTurnDirection(plstdstarlegUpdate.getTurnDirection());
	// existingPlStdStarLeg.setAlt2(plstdstarlegUpdate.getAlt2());
	// existingPlStdStarLeg.setAlt1(plstdstarlegUpdate.getAlt1());
	// existingPlStdStarLeg.setCenterFixSection(plstdstarlegUpdate.getCenterFixSection());
	// existingPlStdStarLeg.setFixIcaoCode(plstdstarlegUpdate.getFixIcaoCode());
	// existingPlStdStarLeg.setSpeedLimit(plstdstarlegUpdate.getSpeedLimit());
	// existingPlStdStarLeg.setStarIdent(plstdstarlegUpdate.getStarIdent());
	// existingPlStdStarLeg.setVerticalAngle(plstdstarlegUpdate.getVerticalAngle());
	// existingPlStdStarLeg.setPathAndTermination(plstdstarlegUpdate.getPathAndTermination());
	// existingPlStdStarLeg.setTransitionIdent(plstdstarlegUpdate.getTransitionIdent());
	// existingPlStdStarLeg.setFixIdent(plstdstarlegUpdate.getFixIdent());
	// existingPlStdStarLeg.setRho(plstdstarlegUpdate.getRho());
	// existingPlStdStarLeg.setAtcInd(plstdstarlegUpdate.getAtcInd());
	// existingPlStdStarLeg.setCycleData(plstdstarlegUpdate.getCycleData());
	// existingPlStdStarLeg.setFileRecno(plstdstarlegUpdate.getFileRecno());
	// existingPlStdStarLeg.setAircraftType(plstdstarlegUpdate.getAircraftType());
	// existingPlStdStarLeg.setWaypointDescCode(plstdstarlegUpdate.getWaypointDescCode());
	// existingPlStdStarLeg.setAirportIcao(plstdstarlegUpdate.getAirportIcao());
	// existingPlStdStarLeg.setProcessingCycle(plstdstarlegUpdate.getProcessingCycle());
	// existingPlStdStarLeg.setTurnDirValid(plstdstarlegUpdate.getTurnDirValid());
	// PlStdStarLeg updatedPlStdStarLeg =
	// plstdstarlegRepository.save(existingPlStdStarLeg);
	// updatedPlStdStarLegs.add(updatedPlStdStarLeg);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while updating PlStdStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS,
	// updatedPlStdStarLegs));
	// }
	//
	// /**
	// * Deletes existing PlStdStarLegs based on the provided list of DTOs.
	// *
	// * @param deleteplstdstarlegs The list of DTOs containing data for deleting
	// PlStdStarLeg.
	// * @return A ResponseDto containing the list of deleted PlStdStarLeg entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarLeg>>>
	// deletePlStdStarLeg(List<PlStdStarLegRequestDto> plstdstarlegDeletes) {
	// BaseResponse<List<PlStdStarLeg>> responseObj = new BaseResponse<>();
	// List<PlStdStarLeg> deletedPlStdStarLegs = new ArrayList<>();
	//
	// for (PlStdStarLegRequestDto plstdstarlegDelete : plstdstarlegDeletes) {
	// try {
	// log.info("Deleting PlStdStarLeg Data");
	// PlStdStarLegIdClass PlStdStarLegId = new
	// PlStdStarLegIdClass(plstdstarlegDelete.getAircraftType(),plstdstarlegDelete.getAirportIcao(),plstdstarlegDelete.getTransitionIdent(),plstdstarlegDelete.getRouteType(),plstdstarlegDelete.getAirportIdent(),plstdstarlegDelete.getSequenceNum(),plstdstarlegDelete.getStarIdent(),plstdstarlegDelete.getDataSupplier(),plstdstarlegDelete.getProcessingCycle());
	// Optional<PlStdStarLeg> existingPlStdStarLegOptional =
	// plstdstarlegRepository.findById(PlStdStarLegId);
	// if (existingPlStdStarLegOptional.isPresent()) {
	// PlStdStarLeg existingPlStdStarLeg = existingPlStdStarLegOptional.get();
	// plstdstarlegRepository.deleteById(PlStdStarLegId);
	// deletedPlStdStarLegs.add(existingPlStdStarLeg);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while deleting PlStdStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE,
	// deletedPlStdStarLegs));
	// }

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarLeg>>> searchPlStdStarLeg(
			PlStdStarLegQuerySearchDto plstdstarlegQuerySearch, int page, int rec) {
		BaseResponse<List<PlStdStarLeg>> responseObj = new BaseResponse<>();
		List<PlStdStarLeg> searchPlStdStarLegs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(plstdstarlegQuerySearch, "pl_std_star_leg", "",
					"route_type, transition_ident, sequence_num", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(plstdstarlegQuerySearch, "pl_std_star_leg", "",
					"route_type, transition_ident, sequence_num", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchPlStdStarLegs.add(app.mapResultSetToClass(searchRec, PlStdStarLeg.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchPlStdStarLegs, total));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarLeg>>> createPlStdStarLeg(
			List<PlStdStarLegRequestDto> plstdstarlegCreateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarLeg>>> updatePlStdStarLeg(
			List<PlStdStarLegRequestDto> plstdstarlegUpdateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarLeg>>> deletePlStdStarLeg(
			List<PlStdStarLegRequestDto> plstdstarlegDeleteRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}
}

-------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarSegmentQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarSegmentRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IPlStdStarSegmentRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IPlStdStarSegmentService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlStdStarSegment Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class PlStdStarSegmentServiceImpl implements IPlStdStarSegmentService {

	@Autowired
	IPlStdStarSegmentRepository plstdstarsegmentRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of PlStdStarSegment with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of PlStdStarSegment based on the
	 *         specified page and rec parameters.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarSegment>>>
	// getAllPlStdStarSegment(int page, int rec) {
	// BaseResponse<List<PlStdStarSegment>> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching all PlStdStarSegment Data");
	// if(page == -1 && rec == -1){
	// List<PlStdStarSegment> plstdstarsegment =
	// plstdstarsegmentRepository.findAll();
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,plstdstarsegment));
	// }
	// Pageable pages = PageRequest.of(page, rec);
	// Page<PlStdStarSegment> plstdstarsegmentPages =
	// plstdstarsegmentRepository.findAll(pages);
	// if(plstdstarsegmentPages.getContent().size() > 0){
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,plstdstarsegmentPages.getContent(),plstdstarsegmentPages.getTotalElements()));
	// } else{
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE,List.of()));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching all PlStdStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Retrieves a specific PlStdStarSegment data by its ID.
	// *
	// * @param id The ID of the PlStdStarSegment to retrieve.
	// * @return A ResponseDto containing the PlStdStarSegment entity with the
	// specified ID.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<PlStdStarSegment>>
	// getPlStdStarSegmentById(Long id) {
	// BaseResponse<PlStdStarSegment> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching PlStdStarSegment Data By Id");
	// Optional<PlStdStarSegment> plstdstarsegment =
	// plstdstarsegmentRepository.findById(id);
	// if (plstdstarsegment.isPresent()) {
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// plstdstarsegment.get()));
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching PlStdStarSegment data by Id",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Creates new PlStdStarSegments based on the provided list of DTOs.
	// *
	// * @param createplstdstarsegments The list of DTOs containing data for
	// creating PlStdStarSegment.
	// * @return A ResponseDto containing the list of created PlStdStarSegment
	// entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarSegment>>>
	// createPlStdStarSegment(List<PlStdStarSegmentRequestDto>
	// plstdstarsegmentsCreate) {
	// BaseResponse<List<PlStdStarSegment>> responseObj = new BaseResponse<>();
	// List<PlStdStarSegment> createdPlStdStarSegments = new ArrayList<>();
	//
	// for (PlStdStarSegmentRequestDto plstdstarsegmentCreate :
	// plstdstarsegmentsCreate) {
	// try {
	// log.info("Creating PlStdStarSegment Data");
	// PlStdStarSegment plstdstarsegment = new PlStdStarSegment();
	// plstdstarsegment.setRouteType(plstdstarsegmentCreate.getRouteType());
	// plstdstarsegment.setAirportIdent(plstdstarsegmentCreate.getAirportIdent());
	// plstdstarsegment.setAirportIcao(plstdstarsegmentCreate.getAirportIcao());
	// plstdstarsegment.setTransitionIdent(plstdstarsegmentCreate.getTransitionIdent());
	// plstdstarsegment.setDataSupplier(plstdstarsegmentCreate.getDataSupplier());
	// plstdstarsegment.setProcessingCycle(plstdstarsegmentCreate.getProcessingCycle());
	// plstdstarsegment.setMaxTransAltitude(plstdstarsegmentCreate.getMaxTransAltitude());
	// plstdstarsegment.setDataSupplier(plstdstarsegmentCreate.getDataSupplier());
	// plstdstarsegment.setRouteType(plstdstarsegmentCreate.getRouteType());
	// plstdstarsegment.setQualifier1(plstdstarsegmentCreate.getQualifier1());
	// plstdstarsegment.setQualifier2(plstdstarsegmentCreate.getQualifier2());
	// plstdstarsegment.setProcDesignMagVarInd(plstdstarsegmentCreate.getProcDesignMagVarInd());
	// plstdstarsegment.setMaxTransAltitude(plstdstarsegmentCreate.getMaxTransAltitude());
	// plstdstarsegment.setAircraftType(plstdstarsegmentCreate.getAircraftType());
	// plstdstarsegment.setAirportIdent(plstdstarsegmentCreate.getAirportIdent());
	// plstdstarsegment.setUpdateDcrNumber(plstdstarsegmentCreate.getUpdateDcrNumber());
	// plstdstarsegment.setStarIdent(plstdstarsegmentCreate.getStarIdent());
	// plstdstarsegment.setAirportIcao(plstdstarsegmentCreate.getAirportIcao());
	// plstdstarsegment.setTransitionIdent(plstdstarsegmentCreate.getTransitionIdent());
	// plstdstarsegment.setProcessingCycle(plstdstarsegmentCreate.getProcessingCycle());
	// plstdstarsegment.setCreateDcrNumber(plstdstarsegmentCreate.getCreateDcrNumber());
	// PlStdStarSegment createdPlStdStarSegment =
	// plstdstarsegmentRepository.save(plstdstarsegment);
	// createdPlStdStarSegments.add(createdPlStdStarSegment);
	// } catch (Exception ex) {
	// log.error("An error occurred while creating PlStdStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED,
	// createdPlStdStarSegments));
	// }
	//
	// /**
	// * Updates existing PlStdStarSegments based on the provided list of DTOs.
	// *
	// * @param plstdstarsegmentsUpdate The list of DTOs containing data for
	// updating PlStdStarSegment.
	// * @return A ResponseDto containing the list of updated PlStdStarSegment
	// entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarSegment>>>
	// updatePlStdStarSegment(List<PlStdStarSegmentRequestDto>
	// plstdstarsegmentsUpdate) {
	// BaseResponse<List<PlStdStarSegment>> responseObj = new BaseResponse<>();
	// List<PlStdStarSegment> updatedPlStdStarSegments = new ArrayList<>();
	//
	// for (PlStdStarSegmentRequestDto plstdstarsegmentUpdate :
	// plstdstarsegmentsUpdate) {
	// try {
	// log.info("Updating PlStdStarSegment Data");
	// PlStdStarSegmentIdClass PlStdStarSegmentId = new
	// PlStdStarSegmentIdClass(plstdstarsegmentUpdate.getDataSupplier(),plstdstarsegmentUpdate.getProcessingCycle(),plstdstarsegmentUpdate.getAircraftType(),plstdstarsegmentUpdate.getTransitionIdent(),plstdstarsegmentUpdate.getAirportIdent(),plstdstarsegmentUpdate.getAirportIcao(),plstdstarsegmentUpdate.getStarIdent(),plstdstarsegmentUpdate.getRouteType());
	// Optional<PlStdStarSegment> existingPlStdStarSegmentOptional =
	// plstdstarsegmentRepository.findById(PlStdStarSegmentId);
	// if (existingPlStdStarSegmentOptional.isPresent()) {
	// PlStdStarSegment existingPlStdStarSegment =
	// existingPlStdStarSegmentOptional.get();
	// existingPlStdStarSegment.setRouteType(plstdstarsegmentUpdate.getRouteType());
	// existingPlStdStarSegment.setAirportIdent(plstdstarsegmentUpdate.getAirportIdent());
	// existingPlStdStarSegment.setAirportIcao(plstdstarsegmentUpdate.getAirportIcao());
	// existingPlStdStarSegment.setTransitionIdent(plstdstarsegmentUpdate.getTransitionIdent());
	// existingPlStdStarSegment.setDataSupplier(plstdstarsegmentUpdate.getDataSupplier());
	// existingPlStdStarSegment.setProcessingCycle(plstdstarsegmentUpdate.getProcessingCycle());
	// existingPlStdStarSegment.setMaxTransAltitude(plstdstarsegmentUpdate.getMaxTransAltitude());
	// existingPlStdStarSegment.setDataSupplier(plstdstarsegmentUpdate.getDataSupplier());
	// existingPlStdStarSegment.setRouteType(plstdstarsegmentUpdate.getRouteType());
	// existingPlStdStarSegment.setQualifier1(plstdstarsegmentUpdate.getQualifier1());
	// existingPlStdStarSegment.setQualifier2(plstdstarsegmentUpdate.getQualifier2());
	// existingPlStdStarSegment.setProcDesignMagVarInd(plstdstarsegmentUpdate.getProcDesignMagVarInd());
	// existingPlStdStarSegment.setMaxTransAltitude(plstdstarsegmentUpdate.getMaxTransAltitude());
	// existingPlStdStarSegment.setAircraftType(plstdstarsegmentUpdate.getAircraftType());
	// existingPlStdStarSegment.setAirportIdent(plstdstarsegmentUpdate.getAirportIdent());
	// existingPlStdStarSegment.setUpdateDcrNumber(plstdstarsegmentUpdate.getUpdateDcrNumber());
	// existingPlStdStarSegment.setStarIdent(plstdstarsegmentUpdate.getStarIdent());
	// existingPlStdStarSegment.setAirportIcao(plstdstarsegmentUpdate.getAirportIcao());
	// existingPlStdStarSegment.setTransitionIdent(plstdstarsegmentUpdate.getTransitionIdent());
	// existingPlStdStarSegment.setProcessingCycle(plstdstarsegmentUpdate.getProcessingCycle());
	// existingPlStdStarSegment.setCreateDcrNumber(plstdstarsegmentUpdate.getCreateDcrNumber());
	// PlStdStarSegment updatedPlStdStarSegment =
	// plstdstarsegmentRepository.save(existingPlStdStarSegment);
	// updatedPlStdStarSegments.add(updatedPlStdStarSegment);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while updating PlStdStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS,
	// updatedPlStdStarSegments));
	// }
	//
	// /**
	// * Deletes existing PlStdStarSegments based on the provided list of DTOs.
	// *
	// * @param deleteplstdstarsegments The list of DTOs containing data for
	// deleting PlStdStarSegment.
	// * @return A ResponseDto containing the list of deleted PlStdStarSegment
	// entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlStdStarSegment>>>
	// deletePlStdStarSegment(List<PlStdStarSegmentRequestDto>
	// plstdstarsegmentDeletes) {
	// BaseResponse<List<PlStdStarSegment>> responseObj = new BaseResponse<>();
	// List<PlStdStarSegment> deletedPlStdStarSegments = new ArrayList<>();
	//
	// for (PlStdStarSegmentRequestDto plstdstarsegmentDelete :
	// plstdstarsegmentDeletes) {
	// try {
	// log.info("Deleting PlStdStarSegment Data");
	// PlStdStarSegmentIdClass PlStdStarSegmentId = new
	// PlStdStarSegmentIdClass(plstdstarsegmentDelete.getDataSupplier(),plstdstarsegmentDelete.getProcessingCycle(),plstdstarsegmentDelete.getAircraftType(),plstdstarsegmentDelete.getTransitionIdent(),plstdstarsegmentDelete.getAirportIdent(),plstdstarsegmentDelete.getAirportIcao(),plstdstarsegmentDelete.getStarIdent(),plstdstarsegmentDelete.getRouteType());
	// Optional<PlStdStarSegment> existingPlStdStarSegmentOptional =
	// plstdstarsegmentRepository.findById(PlStdStarSegmentId);
	// if (existingPlStdStarSegmentOptional.isPresent()) {
	// PlStdStarSegment existingPlStdStarSegment =
	// existingPlStdStarSegmentOptional.get();
	// plstdstarsegmentRepository.deleteById(PlStdStarSegmentId);
	// deletedPlStdStarSegments.add(existingPlStdStarSegment);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while deleting PlStdStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE,
	// deletedPlStdStarSegments));
	// }

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarSegment>>> searchPlStdStarSegment(
			PlStdStarSegmentQuerySearchDto plstdstarsegmentQuerySearch, int page, int rec) {
		BaseResponse<List<PlStdStarSegment>> responseObj = new BaseResponse<>();
		List<PlStdStarSegment> searchPlStdStarSegments = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(plstdstarsegmentQuerySearch, "pl_std_star_segment", "",
					"route_type, transition_ident", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(plstdstarsegmentQuerySearch, "pl_std_star_segment", "",
					"route_type, transition_ident", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchPlStdStarSegments.add(app.mapResultSetToClass(searchRec, PlStdStarSegment.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchPlStdStarSegments, total));
		} catch (Exception e) {
			log.error("An error occurred while searching PlStdStarSegment data", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarSegment>>> getAllPlStdStarSegment(int page, int rec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<PlStdStarSegment>> getPlStdStarSegmentById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarSegment>>> createPlStdStarSegment(
			List<PlStdStarSegmentRequestDto> plstdstarsegmentCreateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarSegment>>> updatePlStdStarSegment(
			List<PlStdStarSegmentRequestDto> plstdstarsegmentUpdateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStarSegment>>> deletePlStdStarSegment(
			List<PlStdStarSegmentRequestDto> plstdstarsegmentDeleteRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}
}

---------------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.AirportStarTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlStdStarRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.response.AirportStarTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStar;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlStdStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.entity.idclass.PlStdStarIdClass;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IPlStdStarRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IPlStdStarService;
import com.honeywell.coreptdu.datatypes.corepttemplate.block.DisplayItemBlock;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.Global;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.oracleutils.Block;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlStdStar Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class PlStdStarServiceImpl implements IPlStdStarService {

	@Autowired
	IPlStdStarRepository plstdstarRepository;

	// @Autowired
	// private IApplication app;

	@Getter
	@Setter
	private Global global = new Global();
	@Getter
	@Setter
	private Block<PlStdStarLeg> plStdStarLeg = new Block<>();
	@Getter
	@Setter
	private PlStdStar plStdStar = new PlStdStar();
	@Getter
	@Setter
	private Block<PlStdStarSegment> plStdStarSegment = new Block<>();

	@Getter
	@Setter
	private DisplayItemBlock displayItemBlock = new DisplayItemBlock();

	@Autowired
	private AirportStarTriggerServiceImpl airportStarTriggerServiceImpl;

	/**
	 * Retrieves a list of PlStdStar with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of PlStdStar based on the specified
	 *         page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<PlStdStar>>> getAllPlStdStar(int page, int rec) {
		BaseResponse<List<PlStdStar>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all PlStdStar Data");
			if (page == -1 && rec == -1) {
				List<PlStdStar> plstdstar = plstdstarRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, plstdstar));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<PlStdStar> plstdstarPages = plstdstarRepository.findAll(pages);
			if (plstdstarPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						plstdstarPages.getContent(), plstdstarPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all PlStdStar data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific PlStdStar data by its ID.
	 *
	 * @param id The ID of the PlStdStar to retrieve.
	 * @return A ResponseDto containing the PlStdStar entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<PlStdStar>> getPlStdStarById(Long id) {
		BaseResponse<PlStdStar> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching PlStdStar Data By Id");
			Optional<PlStdStar> plstdstar = plstdstarRepository.findById(id);
			if (plstdstar.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, plstdstar.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching PlStdStar data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Creates new PlStdStars based on the provided list of DTOs.
	 *
	 * @param createplstdstars The list of DTOs containing data for creating
	 *                         PlStdStar.
	 * @return A ResponseDto containing the list of created PlStdStar entities.
	 */
//	@Override
//	public ResponseEntity<ResponseDto<List<PlStdStar>>> createPlStdStar(List<PlStdStarRequestDto> plstdstarsCreate) {
//		BaseResponse<List<PlStdStar>> responseObj = new BaseResponse<>();
//		List<PlStdStar> createdPlStdStars = new ArrayList<>();
//
//		for (PlStdStarRequestDto plstdstarCreate : plstdstarsCreate) {
//			try {
//				log.info("Creating PlStdStar Data");
//				PlStdStar plstdstar = new PlStdStar();
//				plstdstar.setAirportIcao(plstdstarCreate.getAirportIcao());
//				plstdstar.setAirportIdent(plstdstarCreate.getAirportIdent());
//				plstdstar.setDataSupplier(plstdstarCreate.getDataSupplier());
//				plstdstar.setProcessingCycle(plstdstarCreate.getProcessingCycle());
//				plstdstar.setAreaCode(plstdstarCreate.getAreaCode());
//				plstdstar.setValidateInd(plstdstarCreate.getValidateInd());
//				plstdstar.setCreateDcrNumber(plstdstarCreate.getCreateDcrNumber());
//				plstdstar.setUpdateDcrNumber(plstdstarCreate.getUpdateDcrNumber());
//				plstdstar.setSaaarStarInd(plstdstarCreate.getSaaarStarInd());
//				plstdstar.setDataSupplier(plstdstarCreate.getDataSupplier());
//				plstdstar.setSpecialsInd(plstdstarCreate.getSpecialsInd());
//				plstdstar.setAreaCode(plstdstarCreate.getAreaCode());
//				plstdstar.setAirportIdent(plstdstarCreate.getAirportIdent());
//				plstdstar.setStarType(plstdstarCreate.getStarType());
//				plstdstar.setValidateInd(plstdstarCreate.getValidateInd());
//				plstdstar.setUpdateDcrNumber(plstdstarCreate.getUpdateDcrNumber());
//				plstdstar.setStarIdent(plstdstarCreate.getStarIdent());
//				plstdstar.setAirportIcao(plstdstarCreate.getAirportIcao());
//				plstdstar.setProcessingCycle(plstdstarCreate.getProcessingCycle());
//				plstdstar.setCreateDcrNumber(plstdstarCreate.getCreateDcrNumber());
//				PlStdStar createdPlStdStar = plstdstarRepository.save(plstdstar);
//				createdPlStdStars.add(createdPlStdStar);
//			} catch (Exception ex) {
//				log.error("An error occurred while creating PlStdStar data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
//			}
//		}
//		return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdPlStdStars));
//	}

	/**
	 * Updates existing PlStdStars based on the provided list of DTOs.
	 *
	 * @param plstdstarsUpdate The list of DTOs containing data for updating
	 *                         PlStdStar.
	 * @return A ResponseDto containing the list of updated PlStdStar entities.
	 */
//	@Override
//	public ResponseEntity<ResponseDto<List<PlStdStar>>> updatePlStdStar(List<PlStdStarRequestDto> plstdstarsUpdate) {
//		BaseResponse<List<PlStdStar>> responseObj = new BaseResponse<>();
//		List<PlStdStar> updatedPlStdStars = new ArrayList<>();
//
//		for (PlStdStarRequestDto plstdstarUpdate : plstdstarsUpdate) {
//			try {
//				log.info("Updating PlStdStar Data");
//				PlStdStarIdClass PlStdStarId = new PlStdStarIdClass(plstdstarUpdate.getStarIdent(),
//						plstdstarUpdate.getAirportIdent(), plstdstarUpdate.getDataSupplier(),
//						plstdstarUpdate.getProcessingCycle(), plstdstarUpdate.getAirportIcao());
//				Optional<PlStdStar> existingPlStdStarOptional = plstdstarRepository.findById(PlStdStarId);
//				if (existingPlStdStarOptional.isPresent()) {
//					PlStdStar existingPlStdStar = existingPlStdStarOptional.get();
//					existingPlStdStar.setAirportIcao(plstdstarUpdate.getAirportIcao());
//					existingPlStdStar.setAirportIdent(plstdstarUpdate.getAirportIdent());
//					existingPlStdStar.setDataSupplier(plstdstarUpdate.getDataSupplier());
//					existingPlStdStar.setProcessingCycle(plstdstarUpdate.getProcessingCycle());
//					existingPlStdStar.setAreaCode(plstdstarUpdate.getAreaCode());
//					existingPlStdStar.setValidateInd(plstdstarUpdate.getValidateInd());
//					existingPlStdStar.setCreateDcrNumber(plstdstarUpdate.getCreateDcrNumber());
//					existingPlStdStar.setUpdateDcrNumber(plstdstarUpdate.getUpdateDcrNumber());
//					existingPlStdStar.setSaaarStarInd(plstdstarUpdate.getSaaarStarInd());
//					existingPlStdStar.setDataSupplier(plstdstarUpdate.getDataSupplier());
//					existingPlStdStar.setSpecialsInd(plstdstarUpdate.getSpecialsInd());
//					existingPlStdStar.setAreaCode(plstdstarUpdate.getAreaCode());
//					existingPlStdStar.setAirportIdent(plstdstarUpdate.getAirportIdent());
//					existingPlStdStar.setStarType(plstdstarUpdate.getStarType());
//					existingPlStdStar.setValidateInd(plstdstarUpdate.getValidateInd());
//					existingPlStdStar.setUpdateDcrNumber(plstdstarUpdate.getUpdateDcrNumber());
//					existingPlStdStar.setStarIdent(plstdstarUpdate.getStarIdent());
//					existingPlStdStar.setAirportIcao(plstdstarUpdate.getAirportIcao());
//					existingPlStdStar.setProcessingCycle(plstdstarUpdate.getProcessingCycle());
//					existingPlStdStar.setCreateDcrNumber(plstdstarUpdate.getCreateDcrNumber());
//					PlStdStar updatedPlStdStar = plstdstarRepository.save(existingPlStdStar);
//					updatedPlStdStars.add(updatedPlStdStar);
//				} else {
//					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
//				}
//			} catch (Exception ex) {
//				log.error("An error occurred while updating PlStdStar data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
//			}
//		}
//		return responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedPlStdStars));
//	}

	/**
	 * Deletes existing PlStdStars based on the provided list of DTOs.
	 *
	 * @param deleteplstdstars The list of DTOs containing data for deleting
	 *                         PlStdStar.
	 * @return A ResponseDto containing the list of deleted PlStdStar entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<PlStdStar>>> deletePlStdStar(List<PlStdStarRequestDto> plstdstarDeletes) {
		BaseResponse<List<PlStdStar>> responseObj = new BaseResponse<>();
		List<PlStdStar> deletedPlStdStars = new ArrayList<>();

		for (PlStdStarRequestDto plstdstarDelete : plstdstarDeletes) {
			try {
				log.info("Deleting PlStdStar Data");
				PlStdStarIdClass PlStdStarId = new PlStdStarIdClass(plstdstarDelete.getStarIdent(),
						plstdstarDelete.getAirportIdent(), plstdstarDelete.getDataSupplier(),
						plstdstarDelete.getProcessingCycle(), plstdstarDelete.getAirportIcao());
				Optional<PlStdStar> existingPlStdStarOptional = plstdstarRepository.findById(PlStdStarId);
				if (existingPlStdStarOptional.isPresent()) {
					PlStdStar existingPlStdStar = existingPlStdStarOptional.get();
					plstdstarRepository.deleteById(PlStdStarId);
					deletedPlStdStars.add(existingPlStdStar);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while deleting PlStdStar data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedPlStdStars));
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// searchPlStdStar(
	// AirportStarTriggerRequestDto reqDto, int page, int rec) throws Exception {
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	//
	// try {
	// OracleHelpers.bulkClassMapper(reqDto, this);
	//
	// PlStdStarQuerySearchDto plstdstarQuerySearch = new PlStdStarQuerySearchDto();
	// OracleHelpers.bulkClassMapper(plStdStar, plstdstarQuerySearch);
	//
	// PlStdStarLegQuerySearchDto plstdstarlegQuerySearch = new
	// PlStdStarLegQuerySearchDto();
	// PlStdStarSegmentQuerySearchDto plstdstarsegmentQuerySearch = new
	// PlStdStarSegmentQuerySearchDto();
	//
	// if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
	// plstdstarQuerySearch.setDataSupplier(global.getDataSupplier());
	// if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
	// plstdstarQuerySearch.setProcessingCycle(global.getProcessingCycle());
	//
	// }
	//
	// }
	//
	// Long total = 0L;
	// // Total Count Process
	// String countQuery = app.getQuery(plstdstarQuerySearch, "pl_std_star", "",
	// "airport_ident,airport_icao,star_ident,processing_cycle", true,
	// page == -1 || rec == -1 ? true : false);
	// Record record = app.selectInto(countQuery);
	// total = record.getLong();
	// String searchQuery = app.getQuery(plstdstarQuerySearch, "pl_std_star", "",
	// "airport_ident,airport_icao,star_ident,processing_cycle", false,
	// page == -1 || rec == -1 ? true : false);
	// List<Record> records = null;
	// if (page == -1 || rec == -1) {
	// records = app.executeQuery(searchQuery);
	// } else {
	// int offset = (page - 1) * rec;
	// records = app.executeQuery(searchQuery, offset, rec);
	// }
	//
	// for (Record searchRec : records) {
	// plStdStar = app.mapResultSetToClass(searchRec, PlStdStar.class);
	// }
	//
	// // setting relation
	// {
	// page =-1 ;
	// rec= -1 ;
	// plstdstarlegQuerySearch.setAirportIdent(plStdStar.getAirportIdent());
	// plstdstarlegQuerySearch.setAirportIcao(plStdStar.getAirportIcao());
	// plstdstarlegQuerySearch.setStarIdent(plStdStar.getStarIdent());
	// plstdstarlegQuerySearch.setDataSupplier(plStdStar.getDataSupplier());
	// plstdstarlegQuerySearch.setProcessingCycle(OracleHelpers.toString(
	// plStdStar.getProcessingCycle()));
	//
	//
	// }
	// countQuery
	// =app.getQuery(plstdstarlegQuerySearch,"pl_Std_star_leg","","route_type,
	// transition_ident, sequence_num",true,page==-1||rec==-1?true:false);
	// record = app.selectInto(countQuery);
	// total = record.getLong();
	// searchQuery =
	// app.getQuery(plstdstarlegQuerySearch,"pl_Std_star_leg","","route_type,
	// transition_ident, sequence_num",false,page==-1||rec==-1?true:false);
	// records = null;
	// if(page==-1||rec==-1){
	// records = app.executeQuery(searchQuery);
	// }else{
	// int offset = (page-1)*rec;
	// records = app.executeQuery(searchQuery,offset,rec);
	// }
	// plStdStarLeg = new Block<PlStdStarLeg>();
	// for (Record searchRec : records) {
	// plStdStarLeg.add(app.mapResultSetToClass(searchRec, PlStdStarLeg.class));
	// }
	//
	// {
	// page =-1 ;
	// rec= -1 ;
	// plstdstarsegmentQuerySearch.setAirportIdent(plStdStar.getAirportIdent());
	// plstdstarsegmentQuerySearch.setAirportIcao(plStdStar.getAirportIcao());
	// plstdstarsegmentQuerySearch.setStarIdent(plStdStar.getStarIdent());
	// plstdstarsegmentQuerySearch.setDataSupplier(plStdStar.getDataSupplier());
	// plstdstarsegmentQuerySearch.setProcessingCycle(OracleHelpers.toString(
	// plStdStar.getProcessingCycle()));
	//
	// }
	//
	// countQuery
	// =app.getQuery(plstdstarsegmentQuerySearch,"pl_Std_star_segment","","route_type,
	// transition_ident",true,page==-1||rec==-1?true:false);
	// record = app.selectInto(countQuery);
	// total = record.getLong();
	// searchQuery =
	// app.getQuery(plstdstarsegmentQuerySearch,"pl_Std_star_segment","","route_type,
	// transition_ident",false,page==-1||rec==-1?true:false);
	// records = null;
	// if(page==-1||rec==-1){
	// records = app.executeQuery(searchQuery);
	// }else{
	// int offset = (page-1)*rec;
	// records = app.executeQuery(searchQuery,offset,rec);
	// }
	// plStdStarSegment = new Block<>();
	//
	// for (Record searchRec : records) {
	// plStdStarSegment.add(app.mapResultSetToClass(searchRec,
	// PlStdStarSegment.class));
	// }
	//
	// OracleHelpers.ResponseMapper(this, resDto);
	//
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// resDto));
	// } catch (Exception e) {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> searchPlStdStar(
			AirportStarTriggerRequestDto reqDto, int page, int rec) throws Exception {

		try {
			return airportStarTriggerServiceImpl.searchPlStdStar(reqDto, page, rec);

		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStar>>> createPlStdStar(
			List<PlStdStarRequestDto> plstdstarCreateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlStdStar>>> updatePlStdStar(
			List<PlStdStarRequestDto> plstdstarUpdateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}
}

----------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarLegQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarLegRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IPlTldStarLegRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IPlTldStarLegService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlTldStarLeg Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class PlTldStarLegServiceImpl implements IPlTldStarLegService {

	@Autowired
	IPlTldStarLegRepository pltldstarlegRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of PlTldStarLeg with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of PlTldStarLeg based on the
	 *         specified page and rec parameters.
	 *         //
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarLeg>>> getAllPlTldStarLeg(int
	// page, int rec) {
	// BaseResponse<List<PlTldStarLeg>> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching all PlTldStarLeg Data");
	// if(page == -1 && rec == -1){
	// List<PlTldStarLeg> pltldstarleg = pltldstarlegRepository.findAll();
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,pltldstarleg));
	// }
	// Pageable pages = PageRequest.of(page, rec);
	// Page<PlTldStarLeg> pltldstarlegPages = pltldstarlegRepository.findAll(pages);
	// if(pltldstarlegPages.getContent().size() > 0){
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,pltldstarlegPages.getContent(),pltldstarlegPages.getTotalElements()));
	// } else{
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE,List.of()));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching all PlTldStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Retrieves a specific PlTldStarLeg data by its ID.
	// *
	// * @param id The ID of the PlTldStarLeg to retrieve.
	// * @return A ResponseDto containing the PlTldStarLeg entity with the specified
	// ID.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<PlTldStarLeg>> getPlTldStarLegById(Long id)
	// {
	// BaseResponse<PlTldStarLeg> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching PlTldStarLeg Data By Id");
	// Optional<PlTldStarLeg> pltldstarleg = pltldstarlegRepository.findById(id);
	// if (pltldstarleg.isPresent()) {
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// pltldstarleg.get()));
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching PlTldStarLeg data by Id",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Creates new PlTldStarLegs based on the provided list of DTOs.
	// *
	// * @param createpltldstarlegs The list of DTOs containing data for creating
	// PlTldStarLeg.
	// * @return A ResponseDto containing the list of created PlTldStarLeg entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarLeg>>>
	// createPlTldStarLeg(List<PlTldStarLegRequestDto> pltldstarlegsCreate) {
	// BaseResponse<List<PlTldStarLeg>> responseObj = new BaseResponse<>();
	// List<PlTldStarLeg> createdPlTldStarLegs = new ArrayList<>();
	//
	// for (PlTldStarLegRequestDto pltldstarlegCreate : pltldstarlegsCreate) {
	// try {
	// log.info("Creating PlTldStarLeg Data");
	// PlTldStarLeg pltldstarleg = new PlTldStarLeg();
	// pltldstarleg.setRouteType(pltldstarlegCreate.getRouteType());
	// pltldstarleg.setTransitionIdent(pltldstarlegCreate.getTransitionIdent());
	// pltldstarleg.setAirportIdent(pltldstarlegCreate.getAirportIdent());
	// pltldstarleg.setSequenceNum(pltldstarlegCreate.getSequenceNum());
	// pltldstarleg.setAirportIcao(pltldstarlegCreate.getAirportIcao());
	// pltldstarleg.setProcessingCycle(pltldstarlegCreate.getProcessingCycle());
	// pltldstarleg.setDataSupplier(pltldstarlegCreate.getDataSupplier());
	// pltldstarleg.setWaypointDescCode(pltldstarlegCreate.getWaypointDescCode());
	// pltldstarleg.setTurnDirection(pltldstarlegCreate.getTurnDirection());
	// pltldstarleg.setRnp(pltldstarlegCreate.getRnp());
	// pltldstarleg.setPathAndTermination(pltldstarlegCreate.getPathAndTermination());
	// pltldstarleg.setTurnDirValid(pltldstarlegCreate.getTurnDirValid());
	// pltldstarleg.setArcRadius(pltldstarlegCreate.getArcRadius());
	// pltldstarleg.setTheta(pltldstarlegCreate.getTheta());
	// pltldstarleg.setRho(pltldstarlegCreate.getRho());
	// pltldstarleg.setMagneticCourse(pltldstarlegCreate.getMagneticCourse());
	// pltldstarleg.setRouteDistance(pltldstarlegCreate.getRouteDistance());
	// pltldstarleg.setAltDescription(pltldstarlegCreate.getAltDescription());
	// pltldstarleg.setAtcInd(pltldstarlegCreate.getAtcInd());
	// pltldstarleg.setAlt1(pltldstarlegCreate.getAlt1());
	// pltldstarleg.setAlt2(pltldstarlegCreate.getAlt2());
	// pltldstarleg.setTransAltitude(pltldstarlegCreate.getTransAltitude());
	// pltldstarleg.setFixIdent(pltldstarlegCreate.getFixIdent());
	// pltldstarleg.setSpeedLimit(pltldstarlegCreate.getSpeedLimit());
	// pltldstarleg.setFixIcaoCode(pltldstarlegCreate.getFixIcaoCode());
	// pltldstarleg.setVerticalAngle(pltldstarlegCreate.getVerticalAngle());
	// pltldstarleg.setCenterFixMultipleCode(pltldstarlegCreate.getCenterFixMultipleCode());
	// pltldstarleg.setFixSectionCode(pltldstarlegCreate.getFixSectionCode());
	// pltldstarleg.setFixSubsectionCode(pltldstarlegCreate.getFixSubsectionCode());
	// pltldstarleg.setRecommNavaidIdent(pltldstarlegCreate.getRecommNavaidIdent());
	// pltldstarleg.setRecommNavaidIcaoCode(pltldstarlegCreate.getRecommNavaidIcaoCode());
	// pltldstarleg.setRecommNavaidSection(pltldstarlegCreate.getRecommNavaidSection());
	// pltldstarleg.setRecommNavaidSubsection(pltldstarlegCreate.getRecommNavaidSubsection());
	// pltldstarleg.setCenterFixIdent(pltldstarlegCreate.getCenterFixIdent());
	// pltldstarleg.setCenterFixIcaoCode(pltldstarlegCreate.getCenterFixIcaoCode());
	// pltldstarleg.setCenterFixSection(pltldstarlegCreate.getCenterFixSection());
	// pltldstarleg.setCenterFixSubsection(pltldstarlegCreate.getCenterFixSubsection());
	// pltldstarleg.setFileRecno(pltldstarlegCreate.getFileRecno());
	// pltldstarleg.setCycleData(pltldstarlegCreate.getCycleData());
	// pltldstarleg.setRouteType(pltldstarlegCreate.getRouteType());
	// pltldstarleg.setCenterFixIdent(pltldstarlegCreate.getCenterFixIdent());
	// pltldstarleg.setProcDesignMagVar(pltldstarlegCreate.getProcDesignMagVar());
	// pltldstarleg.setMagneticCourse(pltldstarlegCreate.getMagneticCourse());
	// pltldstarleg.setCustomerIdent(pltldstarlegCreate.getCustomerIdent());
	// pltldstarleg.setCenterFixMultipleCode(pltldstarlegCreate.getCenterFixMultipleCode());
	// pltldstarleg.setAirportIdent(pltldstarlegCreate.getAirportIdent());
	// pltldstarleg.setRnp(pltldstarlegCreate.getRnp());
	// pltldstarleg.setFixSubsectionCode(pltldstarlegCreate.getFixSubsectionCode());
	// pltldstarleg.setCenterFixIcaoCode(pltldstarlegCreate.getCenterFixIcaoCode());
	// pltldstarleg.setRecommNavaidSection(pltldstarlegCreate.getRecommNavaidSection());
	// pltldstarleg.setAltDescription(pltldstarlegCreate.getAltDescription());
	// pltldstarleg.setSpeedLimitDesc(pltldstarlegCreate.getSpeedLimitDesc());
	// pltldstarleg.setArcRadius(pltldstarlegCreate.getArcRadius());
	// pltldstarleg.setValidateInd(pltldstarlegCreate.getValidateInd());
	// pltldstarleg.setUpdateDcrNumber(pltldstarlegCreate.getUpdateDcrNumber());
	// pltldstarleg.setGeneratedInHouseFlag(pltldstarlegCreate.getGeneratedInHouseFlag());
	// pltldstarleg.setTransAltitude(pltldstarlegCreate.getTransAltitude());
	// pltldstarleg.setSequenceNum(pltldstarlegCreate.getSequenceNum());
	// pltldstarleg.setTheta(pltldstarlegCreate.getTheta());
	// pltldstarleg.setCreateDcrNumber(pltldstarlegCreate.getCreateDcrNumber());
	// pltldstarleg.setFixSectionCode(pltldstarlegCreate.getFixSectionCode());
	// pltldstarleg.setDataSupplier(pltldstarlegCreate.getDataSupplier());
	// pltldstarleg.setRouteDistance(pltldstarlegCreate.getRouteDistance());
	// pltldstarleg.setRecommNavaidIdent(pltldstarlegCreate.getRecommNavaidIdent());
	// pltldstarleg.setRecommNavaidSubsection(pltldstarlegCreate.getRecommNavaidSubsection());
	// pltldstarleg.setRecommNavaidIcaoCode(pltldstarlegCreate.getRecommNavaidIcaoCode());
	// pltldstarleg.setCenterFixSubsection(pltldstarlegCreate.getCenterFixSubsection());
	// pltldstarleg.setTurnDirection(pltldstarlegCreate.getTurnDirection());
	// pltldstarleg.setAlt2(pltldstarlegCreate.getAlt2());
	// pltldstarleg.setAlt1(pltldstarlegCreate.getAlt1());
	// pltldstarleg.setCenterFixSection(pltldstarlegCreate.getCenterFixSection());
	// pltldstarleg.setFixIcaoCode(pltldstarlegCreate.getFixIcaoCode());
	// pltldstarleg.setSpeedLimit(pltldstarlegCreate.getSpeedLimit());
	// pltldstarleg.setStarIdent(pltldstarlegCreate.getStarIdent());
	// pltldstarleg.setVerticalAngle(pltldstarlegCreate.getVerticalAngle());
	// pltldstarleg.setPathAndTermination(pltldstarlegCreate.getPathAndTermination());
	// pltldstarleg.setTransitionIdent(pltldstarlegCreate.getTransitionIdent());
	// pltldstarleg.setFixIdent(pltldstarlegCreate.getFixIdent());
	// pltldstarleg.setRho(pltldstarlegCreate.getRho());
	// pltldstarleg.setAtcInd(pltldstarlegCreate.getAtcInd());
	// pltldstarleg.setCycleData(pltldstarlegCreate.getCycleData());
	// pltldstarleg.setFileRecno(pltldstarlegCreate.getFileRecno());
	// pltldstarleg.setAircraftType(pltldstarlegCreate.getAircraftType());
	// pltldstarleg.setWaypointDescCode(pltldstarlegCreate.getWaypointDescCode());
	// pltldstarleg.setAirportIcao(pltldstarlegCreate.getAirportIcao());
	// pltldstarleg.setProcessingCycle(pltldstarlegCreate.getProcessingCycle());
	// pltldstarleg.setTurnDirValid(pltldstarlegCreate.getTurnDirValid());
	// PlTldStarLeg createdPlTldStarLeg = pltldstarlegRepository.save(pltldstarleg);
	// createdPlTldStarLegs.add(createdPlTldStarLeg);
	// } catch (Exception ex) {
	// log.error("An error occurred while creating PlTldStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED,
	// createdPlTldStarLegs));
	// }
	//
	// /**
	// * Updates existing PlTldStarLegs based on the provided list of DTOs.
	// *
	// * @param pltldstarlegsUpdate The list of DTOs containing data for updating
	// PlTldStarLeg.
	// * @return A ResponseDto containing the list of updated PlTldStarLeg entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarLeg>>>
	// updatePlTldStarLeg(List<PlTldStarLegRequestDto> pltldstarlegsUpdate) {
	// BaseResponse<List<PlTldStarLeg>> responseObj = new BaseResponse<>();
	// List<PlTldStarLeg> updatedPlTldStarLegs = new ArrayList<>();
	//
	// for (PlTldStarLegRequestDto pltldstarlegUpdate : pltldstarlegsUpdate) {
	// try {
	// log.info("Updating PlTldStarLeg Data");
	// PlTldStarLegIdClass PlTldStarLegId = new
	// PlTldStarLegIdClass(pltldstarlegUpdate.getAircraftType(),pltldstarlegUpdate.getAirportIcao(),pltldstarlegUpdate.getTransitionIdent(),pltldstarlegUpdate.getRouteType(),pltldstarlegUpdate.getAirportIdent(),pltldstarlegUpdate.getSequenceNum(),pltldstarlegUpdate.getStarIdent(),pltldstarlegUpdate.getDataSupplier(),pltldstarlegUpdate.getProcessingCycle(),pltldstarlegUpdate.getCustomerIdent());
	// Optional<PlTldStarLeg> existingPlTldStarLegOptional =
	// pltldstarlegRepository.findById(PlTldStarLegId);
	// if (existingPlTldStarLegOptional.isPresent()) {
	// PlTldStarLeg existingPlTldStarLeg = existingPlTldStarLegOptional.get();
	// existingPlTldStarLeg.setRouteType(pltldstarlegUpdate.getRouteType());
	// existingPlTldStarLeg.setTransitionIdent(pltldstarlegUpdate.getTransitionIdent());
	// existingPlTldStarLeg.setAirportIdent(pltldstarlegUpdate.getAirportIdent());
	// existingPlTldStarLeg.setSequenceNum(pltldstarlegUpdate.getSequenceNum());
	// existingPlTldStarLeg.setAirportIcao(pltldstarlegUpdate.getAirportIcao());
	// existingPlTldStarLeg.setProcessingCycle(pltldstarlegUpdate.getProcessingCycle());
	// existingPlTldStarLeg.setDataSupplier(pltldstarlegUpdate.getDataSupplier());
	// existingPlTldStarLeg.setWaypointDescCode(pltldstarlegUpdate.getWaypointDescCode());
	// existingPlTldStarLeg.setTurnDirection(pltldstarlegUpdate.getTurnDirection());
	// existingPlTldStarLeg.setRnp(pltldstarlegUpdate.getRnp());
	// existingPlTldStarLeg.setPathAndTermination(pltldstarlegUpdate.getPathAndTermination());
	// existingPlTldStarLeg.setTurnDirValid(pltldstarlegUpdate.getTurnDirValid());
	// existingPlTldStarLeg.setArcRadius(pltldstarlegUpdate.getArcRadius());
	// existingPlTldStarLeg.setTheta(pltldstarlegUpdate.getTheta());
	// existingPlTldStarLeg.setRho(pltldstarlegUpdate.getRho());
	// existingPlTldStarLeg.setMagneticCourse(pltldstarlegUpdate.getMagneticCourse());
	// existingPlTldStarLeg.setRouteDistance(pltldstarlegUpdate.getRouteDistance());
	// existingPlTldStarLeg.setAltDescription(pltldstarlegUpdate.getAltDescription());
	// existingPlTldStarLeg.setAtcInd(pltldstarlegUpdate.getAtcInd());
	// existingPlTldStarLeg.setAlt1(pltldstarlegUpdate.getAlt1());
	// existingPlTldStarLeg.setAlt2(pltldstarlegUpdate.getAlt2());
	// existingPlTldStarLeg.setTransAltitude(pltldstarlegUpdate.getTransAltitude());
	// existingPlTldStarLeg.setFixIdent(pltldstarlegUpdate.getFixIdent());
	// existingPlTldStarLeg.setSpeedLimit(pltldstarlegUpdate.getSpeedLimit());
	// existingPlTldStarLeg.setFixIcaoCode(pltldstarlegUpdate.getFixIcaoCode());
	// existingPlTldStarLeg.setVerticalAngle(pltldstarlegUpdate.getVerticalAngle());
	// existingPlTldStarLeg.setCenterFixMultipleCode(pltldstarlegUpdate.getCenterFixMultipleCode());
	// existingPlTldStarLeg.setFixSectionCode(pltldstarlegUpdate.getFixSectionCode());
	// existingPlTldStarLeg.setFixSubsectionCode(pltldstarlegUpdate.getFixSubsectionCode());
	// existingPlTldStarLeg.setRecommNavaidIdent(pltldstarlegUpdate.getRecommNavaidIdent());
	// existingPlTldStarLeg.setRecommNavaidIcaoCode(pltldstarlegUpdate.getRecommNavaidIcaoCode());
	// existingPlTldStarLeg.setRecommNavaidSection(pltldstarlegUpdate.getRecommNavaidSection());
	// existingPlTldStarLeg.setRecommNavaidSubsection(pltldstarlegUpdate.getRecommNavaidSubsection());
	// existingPlTldStarLeg.setCenterFixIdent(pltldstarlegUpdate.getCenterFixIdent());
	// existingPlTldStarLeg.setCenterFixIcaoCode(pltldstarlegUpdate.getCenterFixIcaoCode());
	// existingPlTldStarLeg.setCenterFixSection(pltldstarlegUpdate.getCenterFixSection());
	// existingPlTldStarLeg.setCenterFixSubsection(pltldstarlegUpdate.getCenterFixSubsection());
	// existingPlTldStarLeg.setFileRecno(pltldstarlegUpdate.getFileRecno());
	// existingPlTldStarLeg.setCycleData(pltldstarlegUpdate.getCycleData());
	// existingPlTldStarLeg.setRouteType(pltldstarlegUpdate.getRouteType());
	// existingPlTldStarLeg.setCenterFixIdent(pltldstarlegUpdate.getCenterFixIdent());
	// existingPlTldStarLeg.setProcDesignMagVar(pltldstarlegUpdate.getProcDesignMagVar());
	// existingPlTldStarLeg.setMagneticCourse(pltldstarlegUpdate.getMagneticCourse());
	// existingPlTldStarLeg.setCustomerIdent(pltldstarlegUpdate.getCustomerIdent());
	// existingPlTldStarLeg.setCenterFixMultipleCode(pltldstarlegUpdate.getCenterFixMultipleCode());
	// existingPlTldStarLeg.setAirportIdent(pltldstarlegUpdate.getAirportIdent());
	// existingPlTldStarLeg.setRnp(pltldstarlegUpdate.getRnp());
	// existingPlTldStarLeg.setFixSubsectionCode(pltldstarlegUpdate.getFixSubsectionCode());
	// existingPlTldStarLeg.setCenterFixIcaoCode(pltldstarlegUpdate.getCenterFixIcaoCode());
	// existingPlTldStarLeg.setRecommNavaidSection(pltldstarlegUpdate.getRecommNavaidSection());
	// existingPlTldStarLeg.setAltDescription(pltldstarlegUpdate.getAltDescription());
	// existingPlTldStarLeg.setSpeedLimitDesc(pltldstarlegUpdate.getSpeedLimitDesc());
	// existingPlTldStarLeg.setArcRadius(pltldstarlegUpdate.getArcRadius());
	// existingPlTldStarLeg.setValidateInd(pltldstarlegUpdate.getValidateInd());
	// existingPlTldStarLeg.setUpdateDcrNumber(pltldstarlegUpdate.getUpdateDcrNumber());
	// existingPlTldStarLeg.setGeneratedInHouseFlag(pltldstarlegUpdate.getGeneratedInHouseFlag());
	// existingPlTldStarLeg.setTransAltitude(pltldstarlegUpdate.getTransAltitude());
	// existingPlTldStarLeg.setSequenceNum(pltldstarlegUpdate.getSequenceNum());
	// existingPlTldStarLeg.setTheta(pltldstarlegUpdate.getTheta());
	// existingPlTldStarLeg.setCreateDcrNumber(pltldstarlegUpdate.getCreateDcrNumber());
	// existingPlTldStarLeg.setFixSectionCode(pltldstarlegUpdate.getFixSectionCode());
	// existingPlTldStarLeg.setDataSupplier(pltldstarlegUpdate.getDataSupplier());
	// existingPlTldStarLeg.setRouteDistance(pltldstarlegUpdate.getRouteDistance());
	// existingPlTldStarLeg.setRecommNavaidIdent(pltldstarlegUpdate.getRecommNavaidIdent());
	// existingPlTldStarLeg.setRecommNavaidSubsection(pltldstarlegUpdate.getRecommNavaidSubsection());
	// existingPlTldStarLeg.setRecommNavaidIcaoCode(pltldstarlegUpdate.getRecommNavaidIcaoCode());
	// existingPlTldStarLeg.setCenterFixSubsection(pltldstarlegUpdate.getCenterFixSubsection());
	// existingPlTldStarLeg.setTurnDirection(pltldstarlegUpdate.getTurnDirection());
	// existingPlTldStarLeg.setAlt2(pltldstarlegUpdate.getAlt2());
	// existingPlTldStarLeg.setAlt1(pltldstarlegUpdate.getAlt1());
	// existingPlTldStarLeg.setCenterFixSection(pltldstarlegUpdate.getCenterFixSection());
	// existingPlTldStarLeg.setFixIcaoCode(pltldstarlegUpdate.getFixIcaoCode());
	// existingPlTldStarLeg.setSpeedLimit(pltldstarlegUpdate.getSpeedLimit());
	// existingPlTldStarLeg.setStarIdent(pltldstarlegUpdate.getStarIdent());
	// existingPlTldStarLeg.setVerticalAngle(pltldstarlegUpdate.getVerticalAngle());
	// existingPlTldStarLeg.setPathAndTermination(pltldstarlegUpdate.getPathAndTermination());
	// existingPlTldStarLeg.setTransitionIdent(pltldstarlegUpdate.getTransitionIdent());
	// existingPlTldStarLeg.setFixIdent(pltldstarlegUpdate.getFixIdent());
	// existingPlTldStarLeg.setRho(pltldstarlegUpdate.getRho());
	// existingPlTldStarLeg.setAtcInd(pltldstarlegUpdate.getAtcInd());
	// existingPlTldStarLeg.setCycleData(pltldstarlegUpdate.getCycleData());
	// existingPlTldStarLeg.setFileRecno(pltldstarlegUpdate.getFileRecno());
	// existingPlTldStarLeg.setAircraftType(pltldstarlegUpdate.getAircraftType());
	// existingPlTldStarLeg.setWaypointDescCode(pltldstarlegUpdate.getWaypointDescCode());
	// existingPlTldStarLeg.setAirportIcao(pltldstarlegUpdate.getAirportIcao());
	// existingPlTldStarLeg.setProcessingCycle(pltldstarlegUpdate.getProcessingCycle());
	// existingPlTldStarLeg.setTurnDirValid(pltldstarlegUpdate.getTurnDirValid());
	// PlTldStarLeg updatedPlTldStarLeg =
	// pltldstarlegRepository.save(existingPlTldStarLeg);
	// updatedPlTldStarLegs.add(updatedPlTldStarLeg);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while updating PlTldStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS,
	// updatedPlTldStarLegs));
	// }
	//
	// /**
	// * Deletes existing PlTldStarLegs based on the provided list of DTOs.
	// *
	// * @param deletepltldstarlegs The list of DTOs containing data for deleting
	// PlTldStarLeg.
	// * @return A ResponseDto containing the list of deleted PlTldStarLeg entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarLeg>>>
	// deletePlTldStarLeg(List<PlTldStarLegRequestDto> pltldstarlegDeletes) {
	// BaseResponse<List<PlTldStarLeg>> responseObj = new BaseResponse<>();
	// List<PlTldStarLeg> deletedPlTldStarLegs = new ArrayList<>();
	//
	// for (PlTldStarLegRequestDto pltldstarlegDelete : pltldstarlegDeletes) {
	// try {
	// log.info("Deleting PlTldStarLeg Data");
	// PlTldStarLegIdClass PlTldStarLegId = new
	// PlTldStarLegIdClass(pltldstarlegDelete.getAircraftType(),pltldstarlegDelete.getAirportIcao(),pltldstarlegDelete.getTransitionIdent(),pltldstarlegDelete.getRouteType(),pltldstarlegDelete.getAirportIdent(),pltldstarlegDelete.getSequenceNum(),pltldstarlegDelete.getStarIdent(),pltldstarlegDelete.getDataSupplier(),pltldstarlegDelete.getProcessingCycle(),pltldstarlegDelete.getCustomerIdent());
	// Optional<PlTldStarLeg> existingPlTldStarLegOptional =
	// pltldstarlegRepository.findById(PlTldStarLegId);
	// if (existingPlTldStarLegOptional.isPresent()) {
	// PlTldStarLeg existingPlTldStarLeg = existingPlTldStarLegOptional.get();
	// pltldstarlegRepository.deleteById(PlTldStarLegId);
	// deletedPlTldStarLegs.add(existingPlTldStarLeg);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while deleting PlTldStarLeg data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE,
	// deletedPlTldStarLegs));
	// }

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarLeg>>> searchPlTldStarLeg(
			PlTldStarLegQuerySearchDto pltldstarlegQuerySearch, int page, int rec) {
		BaseResponse<List<PlTldStarLeg>> responseObj = new BaseResponse<>();
		List<PlTldStarLeg> searchPlTldStarLegs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(pltldstarlegQuerySearch, "pl_tld_star_leg", "",
					"route_type, transition_ident, sequence_num", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(pltldstarlegQuerySearch, "pl_tld_star_leg", "",
					"route_type, transition_ident, sequence_num", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchPlTldStarLegs.add(app.mapResultSetToClass(searchRec, PlTldStarLeg.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchPlTldStarLegs, total));
		} catch (Exception e) {
			log.error("An error occurred while fetching PlTldStarLeg data", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarLeg>>> getAllPlTldStarLeg(int page, int rec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<PlTldStarLeg>> getPlTldStarLegById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarLeg>>> createPlTldStarLeg(
			List<PlTldStarLegRequestDto> pltldstarlegCreateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarLeg>>> updatePlTldStarLeg(
			List<PlTldStarLegRequestDto> pltldstarlegUpdateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarLeg>>> deletePlTldStarLeg(
			List<PlTldStarLegRequestDto> pltldstarlegDeleteRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}
}

----------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarSegmentQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarSegmentRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IPlTldStarSegmentRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IPlTldStarSegmentService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlTldStarSegment Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class PlTldStarSegmentServiceImpl implements IPlTldStarSegmentService {

	@Autowired
	IPlTldStarSegmentRepository pltldstarsegmentRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of PlTldStarSegment with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of PlTldStarSegment based on the
	 *         specified page and rec parameters.
	 */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarSegment>>>
	// getAllPlTldStarSegment(int page, int rec) {
	// BaseResponse<List<PlTldStarSegment>> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching all PlTldStarSegment Data");
	// if(page == -1 && rec == -1){
	// List<PlTldStarSegment> pltldstarsegment =
	// pltldstarsegmentRepository.findAll();
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,pltldstarsegment));
	// }
	// Pageable pages = PageRequest.of(page, rec);
	// Page<PlTldStarSegment> pltldstarsegmentPages =
	// pltldstarsegmentRepository.findAll(pages);
	// if(pltldstarsegmentPages.getContent().size() > 0){
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,pltldstarsegmentPages.getContent(),pltldstarsegmentPages.getTotalElements()));
	// } else{
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE,List.of()));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching all PlTldStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Retrieves a specific PlTldStarSegment data by its ID.
	// *
	// * @param id The ID of the PlTldStarSegment to retrieve.
	// * @return A ResponseDto containing the PlTldStarSegment entity with the
	// specified ID.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<PlTldStarSegment>>
	// getPlTldStarSegmentById(Long id) {
	// BaseResponse<PlTldStarSegment> responseObj = new BaseResponse<>();
	// try {
	// log.info("Fetching PlTldStarSegment Data By Id");
	// Optional<PlTldStarSegment> pltldstarsegment =
	// pltldstarsegmentRepository.findById(id);
	// if (pltldstarsegment.isPresent()) {
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// pltldstarsegment.get()));
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while fetching PlTldStarSegment data by Id",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// }
	//
	// /**
	// * Creates new PlTldStarSegments based on the provided list of DTOs.
	// *
	// * @param createpltldstarsegments The list of DTOs containing data for
	// creating PlTldStarSegment.
	// * @return A ResponseDto containing the list of created PlTldStarSegment
	// entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarSegment>>>
	// createPlTldStarSegment(List<PlTldStarSegmentRequestDto>
	// pltldstarsegmentsCreate) {
	// BaseResponse<List<PlTldStarSegment>> responseObj = new BaseResponse<>();
	// List<PlTldStarSegment> createdPlTldStarSegments = new ArrayList<>();
	//
	// for (PlTldStarSegmentRequestDto pltldstarsegmentCreate :
	// pltldstarsegmentsCreate) {
	// try {
	// log.info("Creating PlTldStarSegment Data");
	// PlTldStarSegment pltldstarsegment = new PlTldStarSegment();
	// pltldstarsegment.setRouteType(pltldstarsegmentCreate.getRouteType());
	// pltldstarsegment.setAirportIdent(pltldstarsegmentCreate.getAirportIdent());
	// pltldstarsegment.setAirportIcao(pltldstarsegmentCreate.getAirportIcao());
	// pltldstarsegment.setTransitionIdent(pltldstarsegmentCreate.getTransitionIdent());
	// pltldstarsegment.setDataSupplier(pltldstarsegmentCreate.getDataSupplier());
	// pltldstarsegment.setProcessingCycle(pltldstarsegmentCreate.getProcessingCycle());
	// pltldstarsegment.setMaxTransAltitude(pltldstarsegmentCreate.getMaxTransAltitude());
	// pltldstarsegment.setDataSupplier(pltldstarsegmentCreate.getDataSupplier());
	// pltldstarsegment.setRouteType(pltldstarsegmentCreate.getRouteType());
	// pltldstarsegment.setQualifier1(pltldstarsegmentCreate.getQualifier1());
	// pltldstarsegment.setQualifier2(pltldstarsegmentCreate.getQualifier2());
	// pltldstarsegment.setProcDesignMagVarInd(pltldstarsegmentCreate.getProcDesignMagVarInd());
	// pltldstarsegment.setMaxTransAltitude(pltldstarsegmentCreate.getMaxTransAltitude());
	// pltldstarsegment.setCustomerIdent(pltldstarsegmentCreate.getCustomerIdent());
	// pltldstarsegment.setAircraftType(pltldstarsegmentCreate.getAircraftType());
	// pltldstarsegment.setAirportIdent(pltldstarsegmentCreate.getAirportIdent());
	// pltldstarsegment.setUpdateDcrNumber(pltldstarsegmentCreate.getUpdateDcrNumber());
	// pltldstarsegment.setStarIdent(pltldstarsegmentCreate.getStarIdent());
	// pltldstarsegment.setAirportIcao(pltldstarsegmentCreate.getAirportIcao());
	// pltldstarsegment.setTransitionIdent(pltldstarsegmentCreate.getTransitionIdent());
	// pltldstarsegment.setGeneratedInHouseFlag(pltldstarsegmentCreate.getGeneratedInHouseFlag());
	// pltldstarsegment.setProcessingCycle(pltldstarsegmentCreate.getProcessingCycle());
	// pltldstarsegment.setCreateDcrNumber(pltldstarsegmentCreate.getCreateDcrNumber());
	// PlTldStarSegment createdPlTldStarSegment =
	// pltldstarsegmentRepository.save(pltldstarsegment);
	// createdPlTldStarSegments.add(createdPlTldStarSegment);
	// } catch (Exception ex) {
	// log.error("An error occurred while creating PlTldStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED,
	// createdPlTldStarSegments));
	// }
	//
	// /**
	// * Updates existing PlTldStarSegments based on the provided list of DTOs.
	// *
	// * @param pltldstarsegmentsUpdate The list of DTOs containing data for
	// updating PlTldStarSegment.
	// * @return A ResponseDto containing the list of updated PlTldStarSegment
	// entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarSegment>>>
	// updatePlTldStarSegment(List<PlTldStarSegmentRequestDto>
	// pltldstarsegmentsUpdate) {
	// BaseResponse<List<PlTldStarSegment>> responseObj = new BaseResponse<>();
	// List<PlTldStarSegment> updatedPlTldStarSegments = new ArrayList<>();
	//
	// for (PlTldStarSegmentRequestDto pltldstarsegmentUpdate :
	// pltldstarsegmentsUpdate) {
	// try {
	// log.info("Updating PlTldStarSegment Data");
	// PlTldStarSegmentIdClass PlTldStarSegmentId = new
	// PlTldStarSegmentIdClass(pltldstarsegmentUpdate.getTransitionIdent(),pltldstarsegmentUpdate.getAirportIdent(),pltldstarsegmentUpdate.getAirportIcao(),pltldstarsegmentUpdate.getStarIdent(),pltldstarsegmentUpdate.getRouteType(),pltldstarsegmentUpdate.getDataSupplier(),pltldstarsegmentUpdate.getProcessingCycle(),pltldstarsegmentUpdate.getCustomerIdent(),pltldstarsegmentUpdate.getAircraftType());
	// Optional<PlTldStarSegment> existingPlTldStarSegmentOptional =
	// pltldstarsegmentRepository.findById(PlTldStarSegmentId);
	// if (existingPlTldStarSegmentOptional.isPresent()) {
	// PlTldStarSegment existingPlTldStarSegment =
	// existingPlTldStarSegmentOptional.get();
	// existingPlTldStarSegment.setRouteType(pltldstarsegmentUpdate.getRouteType());
	// existingPlTldStarSegment.setAirportIdent(pltldstarsegmentUpdate.getAirportIdent());
	// existingPlTldStarSegment.setAirportIcao(pltldstarsegmentUpdate.getAirportIcao());
	// existingPlTldStarSegment.setTransitionIdent(pltldstarsegmentUpdate.getTransitionIdent());
	// existingPlTldStarSegment.setDataSupplier(pltldstarsegmentUpdate.getDataSupplier());
	// existingPlTldStarSegment.setProcessingCycle(pltldstarsegmentUpdate.getProcessingCycle());
	// existingPlTldStarSegment.setMaxTransAltitude(pltldstarsegmentUpdate.getMaxTransAltitude());
	// existingPlTldStarSegment.setDataSupplier(pltldstarsegmentUpdate.getDataSupplier());
	// existingPlTldStarSegment.setRouteType(pltldstarsegmentUpdate.getRouteType());
	// existingPlTldStarSegment.setQualifier1(pltldstarsegmentUpdate.getQualifier1());
	// existingPlTldStarSegment.setQualifier2(pltldstarsegmentUpdate.getQualifier2());
	// existingPlTldStarSegment.setProcDesignMagVarInd(pltldstarsegmentUpdate.getProcDesignMagVarInd());
	// existingPlTldStarSegment.setMaxTransAltitude(pltldstarsegmentUpdate.getMaxTransAltitude());
	// existingPlTldStarSegment.setCustomerIdent(pltldstarsegmentUpdate.getCustomerIdent());
	// existingPlTldStarSegment.setAircraftType(pltldstarsegmentUpdate.getAircraftType());
	// existingPlTldStarSegment.setAirportIdent(pltldstarsegmentUpdate.getAirportIdent());
	// existingPlTldStarSegment.setUpdateDcrNumber(pltldstarsegmentUpdate.getUpdateDcrNumber());
	// existingPlTldStarSegment.setStarIdent(pltldstarsegmentUpdate.getStarIdent());
	// existingPlTldStarSegment.setAirportIcao(pltldstarsegmentUpdate.getAirportIcao());
	// existingPlTldStarSegment.setTransitionIdent(pltldstarsegmentUpdate.getTransitionIdent());
	// existingPlTldStarSegment.setGeneratedInHouseFlag(pltldstarsegmentUpdate.getGeneratedInHouseFlag());
	// existingPlTldStarSegment.setProcessingCycle(pltldstarsegmentUpdate.getProcessingCycle());
	// existingPlTldStarSegment.setCreateDcrNumber(pltldstarsegmentUpdate.getCreateDcrNumber());
	// PlTldStarSegment updatedPlTldStarSegment =
	// pltldstarsegmentRepository.save(existingPlTldStarSegment);
	// updatedPlTldStarSegments.add(updatedPlTldStarSegment);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while updating PlTldStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS,
	// updatedPlTldStarSegments));
	// }
	//
	// /**
	// * Deletes existing PlTldStarSegments based on the provided list of DTOs.
	// *
	// * @param deletepltldstarsegments The list of DTOs containing data for
	// deleting PlTldStarSegment.
	// * @return A ResponseDto containing the list of deleted PlTldStarSegment
	// entities.
	// */
	// @Override
	// public ResponseEntity<ResponseDto<List<PlTldStarSegment>>>
	// deletePlTldStarSegment(List<PlTldStarSegmentRequestDto>
	// pltldstarsegmentDeletes) {
	// BaseResponse<List<PlTldStarSegment>> responseObj = new BaseResponse<>();
	// List<PlTldStarSegment> deletedPlTldStarSegments = new ArrayList<>();
	//
	// for (PlTldStarSegmentRequestDto pltldstarsegmentDelete :
	// pltldstarsegmentDeletes) {
	// try {
	// log.info("Deleting PlTldStarSegment Data");
	// PlTldStarSegmentIdClass PlTldStarSegmentId = new
	// PlTldStarSegmentIdClass(pltldstarsegmentDelete.getTransitionIdent(),pltldstarsegmentDelete.getAirportIdent(),pltldstarsegmentDelete.getAirportIcao(),pltldstarsegmentDelete.getStarIdent(),pltldstarsegmentDelete.getRouteType(),pltldstarsegmentDelete.getDataSupplier(),pltldstarsegmentDelete.getProcessingCycle(),pltldstarsegmentDelete.getCustomerIdent(),pltldstarsegmentDelete.getAircraftType());
	// Optional<PlTldStarSegment> existingPlTldStarSegmentOptional =
	// pltldstarsegmentRepository.findById(PlTldStarSegmentId);
	// if (existingPlTldStarSegmentOptional.isPresent()) {
	// PlTldStarSegment existingPlTldStarSegment =
	// existingPlTldStarSegmentOptional.get();
	// pltldstarsegmentRepository.deleteById(PlTldStarSegmentId);
	// deletedPlTldStarSegments.add(existingPlTldStarSegment);
	// } else {
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
	// }
	// } catch (Exception ex) {
	// log.error("An error occurred while deleting PlTldStarSegment data",
	// ex.getMessage());
	// return
	// responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
	// }
	// }
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE,
	// deletedPlTldStarSegments));
	// }

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarSegment>>> searchPlTldStarSegment(
			PlTldStarSegmentQuerySearchDto pltldstarsegmentQuerySearch, int page, int rec) {
		BaseResponse<List<PlTldStarSegment>> responseObj = new BaseResponse<>();
		List<PlTldStarSegment> searchPlTldStarSegments = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(pltldstarsegmentQuerySearch, "pl_tld_star_segment", "",
					"route_type, transition_ident", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(pltldstarsegmentQuerySearch, "pl_tld_star_segment", "",
					"route_type, transition_ident", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchPlTldStarSegments.add(app.mapResultSetToClass(searchRec, PlTldStarSegment.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchPlTldStarSegments, total));
		} catch (Exception e) {
			log.error("An error occurred while searching PlTldStarSegment data", e.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarSegment>>> getAllPlTldStarSegment(int page, int rec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<PlTldStarSegment>> getPlTldStarSegmentById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarSegment>>> createPlTldStarSegment(
			List<PlTldStarSegmentRequestDto> pltldstarsegmentCreateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarSegment>>> updatePlTldStarSegment(
			List<PlTldStarSegmentRequestDto> pltldstarsegmentUpdateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStarSegment>>> deletePlTldStarSegment(
			List<PlTldStarSegmentRequestDto> pltldstarsegmentDeleteRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}
}

-----------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.AirportStarTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.request.PlTldStarRequestDto;
import com.honeywell.coreptdu.datatypes.airportstar.dto.response.AirportStarTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStar;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.entity.PlTldStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.entity.idclass.PlTldStarIdClass;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IPlTldStarRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IPlTldStarService;
import com.honeywell.coreptdu.datatypes.corepttemplate.block.DisplayItemBlock;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.Global;
import com.honeywell.coreptdu.global.dto.Parameter;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.oracleutils.Block;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * PlTldStar Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class PlTldStarServiceImpl implements IPlTldStarService {

	@Autowired
	IPlTldStarRepository pltldstarRepository;

	// @Autowired
	// private IApplication app;

	@Getter
	@Setter
	private Global global = new Global();
	@Getter
	@Setter
	private Block<PlTldStarLeg> plTldStarLeg = new Block<>();
	@Getter
	@Setter
	private PlTldStar plTldStar = new PlTldStar();
	@Getter
	@Setter
	private Block<PlTldStarSegment> plTldStarSegment = new Block<>();

	@Getter
	@Setter
	private DisplayItemBlock displayItemBlock = new DisplayItemBlock();

	@Getter
	@Setter
	private Parameter parameter = new Parameter();

	@Autowired
	private AirportStarTriggerServiceImpl airportStarTriggerServiceImpl;

	/**
	 * Retrieves a list of PlTldStar with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of PlTldStar based on the specified
	 *         page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<PlTldStar>>> getAllPlTldStar(int page, int rec) {
		BaseResponse<List<PlTldStar>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all PlTldStar Data");
			if (page == -1 && rec == -1) {
				List<PlTldStar> pltldstar = pltldstarRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, pltldstar));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<PlTldStar> pltldstarPages = pltldstarRepository.findAll(pages);
			if (pltldstarPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						pltldstarPages.getContent(), pltldstarPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all PlTldStar data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific PlTldStar data by its ID.
	 *
	 * @param id The ID of the PlTldStar to retrieve.
	 * @return A ResponseDto containing the PlTldStar entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<PlTldStar>> getPlTldStarById(Long id) {
		BaseResponse<PlTldStar> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching PlTldStar Data By Id");
			Optional<PlTldStar> pltldstar = pltldstarRepository.findById(id);
			if (pltldstar.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, pltldstar.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching PlTldStar data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Creates new PlTldStars based on the provided list of DTOs.
	 *
	 * @param createpltldstars The list of DTOs containing data for creating
	 *                         PlTldStar.
	 * @return A ResponseDto containing the list of created PlTldStar entities.
	 */
//	@Override
//	public ResponseEntity<ResponseDto<List<PlTldStar>>> createPlTldStar(List<PlTldStarRequestDto> pltldstarsCreate) {
//		BaseResponse<List<PlTldStar>> responseObj = new BaseResponse<>();
//		List<PlTldStar> createdPlTldStars = new ArrayList<>();
//
//		for (PlTldStarRequestDto pltldstarCreate : pltldstarsCreate) {
//			try {
//				log.info("Creating PlTldStar Data");
//				PlTldStar pltldstar = new PlTldStar();
//				pltldstar.setAirportIcao(pltldstarCreate.getAirportIcao());
//				pltldstar.setAirportIdent(pltldstarCreate.getAirportIdent());
//				pltldstar.setDataSupplier(pltldstarCreate.getDataSupplier());
//				pltldstar.setProcessingCycle(pltldstarCreate.getProcessingCycle());
//				pltldstar.setValidateInd(pltldstarCreate.getValidateInd());
//				pltldstar.setCreateDcrNumber(pltldstarCreate.getCreateDcrNumber());
//				pltldstar.setUpdateDcrNumber(pltldstarCreate.getUpdateDcrNumber());
//				pltldstar.setSaaarStarInd(pltldstarCreate.getSaaarStarInd());
//				pltldstar.setDataSupplier(pltldstarCreate.getDataSupplier());
//				pltldstar.setSpecialsInd(pltldstarCreate.getSpecialsInd());
//				pltldstar.setCustomerIdent(pltldstarCreate.getCustomerIdent());
//				pltldstar.setAirportIdent(pltldstarCreate.getAirportIdent());
//				pltldstar.setStarType(pltldstarCreate.getStarType());
//				pltldstar.setValidateInd(pltldstarCreate.getValidateInd());
//				pltldstar.setUpdateDcrNumber(pltldstarCreate.getUpdateDcrNumber());
//				pltldstar.setStarIdent(pltldstarCreate.getStarIdent());
//				pltldstar.setAirportIcao(pltldstarCreate.getAirportIcao());
//				pltldstar.setGeneratedInHouseFlag(pltldstarCreate.getGeneratedInHouseFlag());
//				pltldstar.setProcessingCycle(pltldstarCreate.getProcessingCycle());
//				pltldstar.setCreateDcrNumber(pltldstarCreate.getCreateDcrNumber());
//				PlTldStar createdPlTldStar = pltldstarRepository.save(pltldstar);
//				createdPlTldStars.add(createdPlTldStar);
//			} catch (Exception ex) {
//				log.error("An error occurred while creating PlTldStar data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_CREATED));
//			}
//		}
//		return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdPlTldStars));
//	}

	/**
	 * Updates existing PlTldStars based on the provided list of DTOs.
	 *
	 * @param pltldstarsUpdate The list of DTOs containing data for updating
	 *                         PlTldStar.
	 * @return A ResponseDto containing the list of updated PlTldStar entities.
	 */
//	@Override
//	public ResponseEntity<ResponseDto<List<PlTldStar>>> updatePlTldStar(List<PlTldStarRequestDto> pltldstarsUpdate) {
//		BaseResponse<List<PlTldStar>> responseObj = new BaseResponse<>();
//		List<PlTldStar> updatedPlTldStars = new ArrayList<>();
//
//		for (PlTldStarRequestDto pltldstarUpdate : pltldstarsUpdate) {
//			try {
//				log.info("Updating PlTldStar Data");
//				PlTldStarIdClass PlTldStarId = new PlTldStarIdClass(pltldstarUpdate.getAirportIcao(),
//						pltldstarUpdate.getStarIdent(), pltldstarUpdate.getAirportIdent(),
//						pltldstarUpdate.getDataSupplier(), pltldstarUpdate.getProcessingCycle(),
//						pltldstarUpdate.getCustomerIdent());
//				Optional<PlTldStar> existingPlTldStarOptional = pltldstarRepository.findById(PlTldStarId);
//				if (existingPlTldStarOptional.isPresent()) {
//					PlTldStar existingPlTldStar = existingPlTldStarOptional.get();
//					existingPlTldStar.setAirportIcao(pltldstarUpdate.getAirportIcao());
//					existingPlTldStar.setAirportIdent(pltldstarUpdate.getAirportIdent());
//					existingPlTldStar.setDataSupplier(pltldstarUpdate.getDataSupplier());
//					existingPlTldStar.setProcessingCycle(pltldstarUpdate.getProcessingCycle());
//					existingPlTldStar.setValidateInd(pltldstarUpdate.getValidateInd());
//					existingPlTldStar.setCreateDcrNumber(pltldstarUpdate.getCreateDcrNumber());
//					existingPlTldStar.setUpdateDcrNumber(pltldstarUpdate.getUpdateDcrNumber());
//					existingPlTldStar.setSaaarStarInd(pltldstarUpdate.getSaaarStarInd());
//					existingPlTldStar.setDataSupplier(pltldstarUpdate.getDataSupplier());
//					existingPlTldStar.setSpecialsInd(pltldstarUpdate.getSpecialsInd());
//					existingPlTldStar.setCustomerIdent(pltldstarUpdate.getCustomerIdent());
//					existingPlTldStar.setAirportIdent(pltldstarUpdate.getAirportIdent());
//					existingPlTldStar.setStarType(pltldstarUpdate.getStarType());
//					existingPlTldStar.setValidateInd(pltldstarUpdate.getValidateInd());
//					existingPlTldStar.setUpdateDcrNumber(pltldstarUpdate.getUpdateDcrNumber());
//					existingPlTldStar.setStarIdent(pltldstarUpdate.getStarIdent());
//					existingPlTldStar.setAirportIcao(pltldstarUpdate.getAirportIcao());
//					existingPlTldStar.setGeneratedInHouseFlag(pltldstarUpdate.getGeneratedInHouseFlag());
//					existingPlTldStar.setProcessingCycle(pltldstarUpdate.getProcessingCycle());
//					existingPlTldStar.setCreateDcrNumber(pltldstarUpdate.getCreateDcrNumber());
//					PlTldStar updatedPlTldStar = pltldstarRepository.save(existingPlTldStar);
//					updatedPlTldStars.add(updatedPlTldStar);
//				} else {
//					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
//				}
//			} catch (Exception ex) {
//				log.error("An error occurred while updating PlTldStar data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
//			}
//		}
//		return responseObj.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedPlTldStars));
//	}

	/**
	 * Deletes existing PlTldStars based on the provided list of DTOs.
	 *
	 * @param deletepltldstars The list of DTOs containing data for deleting
	 *                         PlTldStar.
	 * @return A ResponseDto containing the list of deleted PlTldStar entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<PlTldStar>>> deletePlTldStar(List<PlTldStarRequestDto> pltldstarDeletes) {
		BaseResponse<List<PlTldStar>> responseObj = new BaseResponse<>();
		List<PlTldStar> deletedPlTldStars = new ArrayList<>();

		for (PlTldStarRequestDto pltldstarDelete : pltldstarDeletes) {
			try {
				log.info("Deleting PlTldStar Data");
				PlTldStarIdClass PlTldStarId = new PlTldStarIdClass(pltldstarDelete.getAirportIcao(),
						pltldstarDelete.getStarIdent(), pltldstarDelete.getAirportIdent(),
						pltldstarDelete.getDataSupplier(), pltldstarDelete.getProcessingCycle(),
						pltldstarDelete.getCustomerIdent());
				Optional<PlTldStar> existingPlTldStarOptional = pltldstarRepository.findById(PlTldStarId);
				if (existingPlTldStarOptional.isPresent()) {
					PlTldStar existingPlTldStar = existingPlTldStarOptional.get();
					pltldstarRepository.deleteById(PlTldStarId);
					deletedPlTldStars.add(existingPlTldStar);
				} else {
					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
				}
			} catch (Exception ex) {
				log.error("An error occurred while deleting PlTldStar data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
			}
		}
		return responseObj.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedPlTldStars));
	}

	// @Override
	// public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>>
	// searchPlTldStar(AirportStarTriggerRequestDto reqDto,int page,int rec) throws
	// Exception {
	// BaseResponse<AirportStarTriggerResponseDto> responseObj = new
	// BaseResponse<>();
	// AirportStarTriggerResponseDto resDto = new AirportStarTriggerResponseDto();
	// try{
	// OracleHelpers.bulkClassMapper(reqDto, this);
	//
	// PlTldStarQuerySearchDto pltldstarQuerySearch = new PlTldStarQuerySearchDto();
	// OracleHelpers.bulkClassMapper(plTldStar, pltldstarQuerySearch);
	// PlTldStarLegQuerySearchDto pltldstarlegQuerySearch = new
	// PlTldStarLegQuerySearchDto();
	// PlTldStarSegmentQuerySearchDto pltldstarsegmentQuerySearch = new
	// PlTldStarSegmentQuerySearchDto();
	// {
	// // prequery block
	// if (!Objects.equals(global.getFromErrorSummary(), "Y")) {
	// pltldstarQuerySearch.setDataSupplier(global.getDataSupplier());
	// if (Objects.equals(displayItemBlock.getAllowBothCycles(), "N")) {
	// pltldstarQuerySearch.setProcessingCycle(global.getProcessingCycle());
	//
	// }
	//
	// }
	// }
	//
	//
	// Long total = 0L;
	//
	// // Total Count Process
	// String countQuery
	// =app.getQuery(pltldstarQuerySearch,"pl_tld_star","","customer_ident,airport_ident,airport_icao,star_ident,processing_cycle",true,page==-1||rec==-1?true:false);
	// Record record = app.selectInto(countQuery);
	// total = record.getLong();
	// String searchQuery =
	// app.getQuery(pltldstarQuerySearch,"pl_tld_star","","customer_ident,airport_ident,airport_icao,star_ident,processing_cycle",false,page==-1||rec==-1?true:false);
	// List<Record> records = null;
	// if(page==-1||rec==-1){
	// records = app.executeQuery(searchQuery);
	//
	// }else{
	// int offset = (page-1)*rec;
	// records = app.executeQuery(searchQuery,offset,rec);
	// }
	//
	// for (Record searchRec : records) {
	// this.plTldStar= app.mapResultSetToClass(searchRec, PlTldStar.class);
	//
	// }
	//
	//
	// // setting relation
	// {
	// page =-1 ;
	// rec= -1 ;
	// pltldstarlegQuerySearch.setAirportIdent(plTldStar.getAirportIdent());
	// pltldstarlegQuerySearch.setAirportIcao(plTldStar.getAirportIcao());
	// pltldstarlegQuerySearch.setStarIdent(plTldStar.getStarIdent());
	// pltldstarlegQuerySearch.setDataSupplier(plTldStar.getDataSupplier());
	// pltldstarlegQuerySearch.setProcessingCycle(OracleHelpers.toString(
	// plTldStar.getProcessingCycle()));
	// pltldstarlegQuerySearch.setCustomerIdent(OracleHelpers.toString(
	// plTldStar.getCustomerIdent()));
	//
	// }
	// countQuery
	// =app.getQuery(pltldstarlegQuerySearch,"pl_tld_star_leg","","route_type,
	// transition_ident, sequence_num",true,page==-1||rec==-1?true:false);
	// record = app.selectInto(countQuery);
	// total = record.getLong();
	// searchQuery =
	// app.getQuery(pltldstarlegQuerySearch,"pl_tld_star_leg","","route_type,
	// transition_ident, sequence_num",false,page==-1||rec==-1?true:false);
	// records = null;
	// if(page==-1||rec==-1){
	// records = app.executeQuery(searchQuery);
	// }else{
	// int offset = (page-1)*rec;
	// records = app.executeQuery(searchQuery,offset,rec);
	// }
	// plTldStarLeg = new Block<PlTldStarLeg>();
	// for (Record searchRec : records) {
	// plTldStarLeg.add(app.mapResultSetToClass(searchRec, PlTldStarLeg.class));
	// }
	//
	// {
	// page =-1 ;
	// rec= -1 ;
	// pltldstarsegmentQuerySearch.setAirportIdent(plTldStar.getAirportIdent());
	// pltldstarsegmentQuerySearch.setAirportIcao(plTldStar.getAirportIcao());
	// pltldstarsegmentQuerySearch.setStarIdent(plTldStar.getStarIdent());
	// pltldstarsegmentQuerySearch.setDataSupplier(plTldStar.getDataSupplier());
	// pltldstarsegmentQuerySearch.setProcessingCycle(OracleHelpers.toString(
	// plTldStar.getProcessingCycle()));
	// pltldstarsegmentQuerySearch.setCustomerIdent(OracleHelpers.toString(
	// plTldStar.getCustomerIdent()));
	//
	// }
	//
	// countQuery
	// =app.getQuery(pltldstarsegmentQuerySearch,"pl_tld_star_segment","","route_type,
	// transition_ident",true,page==-1||rec==-1?true:false);
	// record = app.selectInto(countQuery);
	// total = record.getLong();
	// searchQuery =
	// app.getQuery(pltldstarsegmentQuerySearch,"pl_tld_star_segment","","route_type,
	// transition_ident",false,page==-1||rec==-1?true:false);
	// records = null;
	// if(page==-1||rec==-1){
	// records = app.executeQuery(searchQuery);
	// }else{
	// int offset = (page-1)*rec;
	// records = app.executeQuery(searchQuery,offset,rec);
	// }
	// plTldStarSegment = new Block<>();
	//
	// for (Record searchRec : records) {
	// plTldStarSegment.add(app.mapResultSetToClass(searchRec,
	// PlTldStarSegment.class));
	// }
	// OracleHelpers.ResponseMapper(this, resDto);
	//
	// return
	// responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
	// resDto));
	// }
	// catch(Exception e){
	// OracleHelpers.ResponseMapper(this, resDto);
	// return ExceptionUtils.handleException(e, resDto);}
	// }

	@Override
	public ResponseEntity<ResponseDto<AirportStarTriggerResponseDto>> searchPlTldStar(
			AirportStarTriggerRequestDto reqDto, int page, int rec) throws Exception {

		try {
			return airportStarTriggerServiceImpl.searchPlTldStar(reqDto, page, rec);

		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStar>>> createPlTldStar(
			List<PlTldStarRequestDto> pltldstarCreateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<ResponseDto<List<PlTldStar>>> updatePlTldStar(
			List<PlTldStarRequestDto> pltldstarUpdateRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}
}

----------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.StdStarLegQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IStdStarLegRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IStdStarLegService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * StdStarLeg Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class StdStarLegServiceImpl implements IStdStarLegService {

	@Autowired
	IStdStarLegRepository stdstarlegRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of StdStarLeg with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of StdStarLeg based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<StdStarLeg>>> getAllStdStarLeg(int page, int rec) {
		BaseResponse<List<StdStarLeg>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all StdStarLeg Data");
			if (page == -1 && rec == -1) {
				List<StdStarLeg> stdstarleg = stdstarlegRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, stdstarleg));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<StdStarLeg> stdstarlegPages = stdstarlegRepository.findAll(pages);
			if (stdstarlegPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						stdstarlegPages.getContent(), stdstarlegPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all StdStarLeg data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific StdStarLeg data by its ID.
	 *
	 * @param id The ID of the StdStarLeg to retrieve.
	 * @return A ResponseDto containing the StdStarLeg entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<StdStarLeg>> getStdStarLegById(Long id) {
		BaseResponse<StdStarLeg> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching StdStarLeg Data By Id");
			Optional<StdStarLeg> stdstarleg = stdstarlegRepository.findById(id);
			if (stdstarleg.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, stdstarleg.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching StdStarLeg data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<StdStarLeg>>> searchStdStarLeg(
			StdStarLegQuerySearchDto stdstarlegQuerySearch, int page, int rec) {
		BaseResponse<List<StdStarLeg>> responseObj = new BaseResponse<>();
		List<StdStarLeg> searchStdStarLegs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(stdstarlegQuerySearch, "std_star_leg", "",
					"route_type, transition_ident, sequence_num", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(stdstarlegQuerySearch, "std_star_leg", "",
					"route_type, transition_ident, sequence_num", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchStdStarLegs.add(app.mapResultSetToClass(searchRec, StdStarLeg.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchStdStarLegs, total));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}

-------------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.StdStarSegmentQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IStdStarSegmentRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IStdStarSegmentService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * StdStarSegment Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class StdStarSegmentServiceImpl implements IStdStarSegmentService {

	@Autowired
	IStdStarSegmentRepository stdstarsegmentRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of StdStarSegment with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of StdStarSegment based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<StdStarSegment>>> getAllStdStarSegment(int page, int rec) {
		BaseResponse<List<StdStarSegment>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all StdStarSegment Data");
			if (page == -1 && rec == -1) {
				List<StdStarSegment> stdstarsegment = stdstarsegmentRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, stdstarsegment));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<StdStarSegment> stdstarsegmentPages = stdstarsegmentRepository.findAll(pages);
			if (stdstarsegmentPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						stdstarsegmentPages.getContent(), stdstarsegmentPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all StdStarSegment data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific StdStarSegment data by its ID.
	 *
	 * @param id The ID of the StdStarSegment to retrieve.
	 * @return A ResponseDto containing the StdStarSegment entity with the specified
	 *         ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<StdStarSegment>> getStdStarSegmentById(Long id) {
		BaseResponse<StdStarSegment> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching StdStarSegment Data By Id");
			Optional<StdStarSegment> stdstarsegment = stdstarsegmentRepository.findById(id);
			if (stdstarsegment.isPresent()) {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, stdstarsegment.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching StdStarSegment data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<StdStarSegment>>> searchStdStarSegment(
			StdStarSegmentQuerySearchDto stdstarsegmentQuerySearch, int page, int rec) {
		BaseResponse<List<StdStarSegment>> responseObj = new BaseResponse<>();
		List<StdStarSegment> searchStdStarSegments = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(stdstarsegmentQuerySearch, "std_star_segment", "",
					"route_type, transition_ident", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(stdstarsegmentQuerySearch, "std_star_segment", "",
					"route_type, transition_ident", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchStdStarSegments.add(app.mapResultSetToClass(searchRec, StdStarSegment.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchStdStarSegments, total));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}

----------------------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.lang.reflect.Field;
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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.StdStarQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.StdStar;
import com.honeywell.coreptdu.datatypes.airportstar.repository.IStdStarRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.IStdStarService;
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
 * StdStar Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class StdStarServiceImpl implements IStdStarService {

	@Autowired
	IStdStarRepository stdstarRepository;

	@Autowired
	private IApplication app;
	
	@Autowired
	private HashUtils hashUtils;

	/**
	 * Retrieves a list of StdStar with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of StdStar based on the specified
	 *         page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<StdStar>>> getAllStdStar(int page, int rec) {
		BaseResponse<List<StdStar>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all StdStar Data");
			if (page == -1 && rec == -1) {
				List<StdStar> stdstar = stdstarRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, stdstar));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<StdStar> stdstarPages = stdstarRepository.findAll(pages);
			if (stdstarPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						stdstarPages.getContent(), stdstarPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all StdStar data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific StdStar data by its ID.
	 *
	 * @param id The ID of the StdStar to retrieve.
	 * @return A ResponseDto containing the StdStar entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<StdStar>> getStdStarById(Long id) {
		BaseResponse<StdStar> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching StdStar Data By Id");
			Optional<StdStar> stdstar = stdstarRepository.findById(id);
			if (stdstar.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, stdstar.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching StdStar data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<StdStar>>> searchStdStar(StdStarQuerySearchDto stdstarQuerySearch, int page,
			int rec) {
		BaseResponse<List<StdStar>> responseObj = new BaseResponse<>();
		List<StdStar> searchStdStars = new ArrayList<>();

		try {
			Long total = 0L;
			String searchQuery = "";
			String sanitizedlastWhere = "";
			String sanitizedcountQuery = "";
			Field _queryField = OracleHelpers.getField(stdstarQuerySearch, "lastWhere");
			if (_queryField != null) {
				_queryField.setAccessible(true);
				Object value = _queryField.get(stdstarQuerySearch);
//					sanitizedqueryWhere = (String) value;
				sanitizedlastWhere = app.sanitizeValueCheck((String) value);
			}
			// Total Count Process
			if (OracleHelpers.isNullorEmpty(sanitizedlastWhere)) {
				String countQuery = app.getQuery(stdstarQuerySearch, "std_star", "",
						"airport_ident,airport_icao,star_ident", true, page == -1 || rec == -1 ? true : false);
				Record record = app.selectInto(countQuery);
				total = record.getLong();
				searchQuery = app.getQuery(stdstarQuerySearch, "std_star", "", "airport_ident,airport_icao,star_ident",
						false, page == -1 || rec == -1 ? true : false);
				List<Record> records = null;
				if (page == -1 || rec == -1) {
					records = app.executeQuery(searchQuery);
				} else {
					int offset = page;
					records = app.executeQuery(searchQuery, offset, rec);
				}

				for (Record searchRec : records) {
					searchStdStars.add(app.mapResultSetToClass(searchRec, StdStar.class));
				}
			} else {
				String countQuery = app.getQuery(stdstarQuerySearch, "std_star", sanitizedlastWhere,
						"airport_ident,airport_icao,star_ident", true, page == -1 || rec == -1 ? true : false);
				sanitizedcountQuery = app.sanitizeValueCheck(countQuery);
				Record record = app.selectInto(sanitizedcountQuery);
				total = record.getLong();
				searchQuery = app.getQuery(stdstarQuerySearch, "std_star", sanitizedlastWhere,
						"airport_ident,airport_icao,star_ident", false, page == -1 || rec == -1 ? true : false);
				List<Record> records = null;
				if (page == -1 || rec == -1) {
					records = app.executeQuery(searchQuery);
				} else {
					int offset = page;
					records = app.executeQuery(searchQuery, offset, rec);
				}

				for (Record searchRec : records) {
					searchStdStars.add(app.mapResultSetToClass(searchRec, StdStar.class));
				}
			}
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchStdStars, total,
					hashUtils.encrypt(searchQuery)));
		}catch (RuntimeException e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		} 
		catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}

------------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.TldStarLegQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStarLeg;
import com.honeywell.coreptdu.datatypes.airportstar.repository.ITldStarLegRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.ITldStarLegService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * TldStarLeg Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class TldStarLegServiceImpl implements ITldStarLegService {

	@Autowired
	ITldStarLegRepository tldstarlegRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of TldStarLeg with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of TldStarLeg based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<TldStarLeg>>> getAllTldStarLeg(int page, int rec) {
		BaseResponse<List<TldStarLeg>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all TldStarLeg Data");
			if (page == -1 && rec == -1) {
				List<TldStarLeg> tldstarleg = tldstarlegRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, tldstarleg));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<TldStarLeg> tldstarlegPages = tldstarlegRepository.findAll(pages);
			if (tldstarlegPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						tldstarlegPages.getContent(), tldstarlegPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all TldStarLeg data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific TldStarLeg data by its ID.
	 *
	 * @param id The ID of the TldStarLeg to retrieve.
	 * @return A ResponseDto containing the TldStarLeg entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<TldStarLeg>> getTldStarLegById(Long id) {
		BaseResponse<TldStarLeg> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching TldStarLeg Data By Id");
			Optional<TldStarLeg> tldstarleg = tldstarlegRepository.findById(id);
			if (tldstarleg.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, tldstarleg.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching TldStarLeg data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<TldStarLeg>>> searchTldStarLeg(
			TldStarLegQuerySearchDto tldstarlegQuerySearch, int page, int rec) {
		BaseResponse<List<TldStarLeg>> responseObj = new BaseResponse<>();
		List<TldStarLeg> searchTldStarLegs = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(tldstarlegQuerySearch, "tld_star_leg", "",
					"route_type, transition_ident, sequence_num", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(tldstarlegQuerySearch, "tld_star_leg", "",
					"route_type, transition_ident, sequence_num", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchTldStarLegs.add(app.mapResultSetToClass(searchRec, TldStarLeg.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchTldStarLegs, total));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}

----------------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.TldStarSegmentQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStarSegment;
import com.honeywell.coreptdu.datatypes.airportstar.repository.ITldStarSegmentRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.ITldStarSegmentService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * TldStarSegment Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class TldStarSegmentServiceImpl implements ITldStarSegmentService {

	@Autowired
	ITldStarSegmentRepository tldstarsegmentRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of TldStarSegment with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of TldStarSegment based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<TldStarSegment>>> getAllTldStarSegment(int page, int rec) {
		BaseResponse<List<TldStarSegment>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all TldStarSegment Data");
			if (page == -1 && rec == -1) {
				List<TldStarSegment> tldstarsegment = tldstarsegmentRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, tldstarsegment));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<TldStarSegment> tldstarsegmentPages = tldstarsegmentRepository.findAll(pages);
			if (tldstarsegmentPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						tldstarsegmentPages.getContent(), tldstarsegmentPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all TldStarSegment data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific TldStarSegment data by its ID.
	 *
	 * @param id The ID of the TldStarSegment to retrieve.
	 * @return A ResponseDto containing the TldStarSegment entity with the specified
	 *         ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<TldStarSegment>> getTldStarSegmentById(Long id) {
		BaseResponse<TldStarSegment> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching TldStarSegment Data By Id");
			Optional<TldStarSegment> tldstarsegment = tldstarsegmentRepository.findById(id);
			if (tldstarsegment.isPresent()) {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, tldstarsegment.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching TldStarSegment data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<TldStarSegment>>> searchTldStarSegment(
			TldStarSegmentQuerySearchDto tldstarsegmentQuerySearch, int page, int rec) {
		BaseResponse<List<TldStarSegment>> responseObj = new BaseResponse<>();
		List<TldStarSegment> searchTldStarSegments = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(tldstarsegmentQuerySearch, "tld_star_segment", "",
					"route_type, transition_ident", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(tldstarsegmentQuerySearch, "tld_star_segment", "",
					"route_type, transition_ident", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				int offset = (page - 1) * rec;
				records = app.executeQuery(searchQuery, offset, rec);
			}

			for (Record searchRec : records) {
				searchTldStarSegments.add(app.mapResultSetToClass(searchRec, TldStarSegment.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchTldStarSegments, total));
		} catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}
---------------------------------
package com.honeywell.coreptdu.datatypes.airportstar.serviceimpl;

import java.lang.reflect.Field;
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

import com.honeywell.coreptdu.datatypes.airportstar.dto.request.TldStarQuerySearchDto;
import com.honeywell.coreptdu.datatypes.airportstar.entity.TldStar;
import com.honeywell.coreptdu.datatypes.airportstar.repository.ITldStarRepository;
import com.honeywell.coreptdu.datatypes.airportstar.service.ITldStarService;
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
 * TldStar Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class TldStarServiceImpl implements ITldStarService {

	@Autowired
	ITldStarRepository tldstarRepository;

	@Autowired
	private IApplication app;
	
	@Autowired
	private HashUtils hashUtils;
	
	

	/**
	 * Retrieves a list of TldStar with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of TldStar based on the specified
	 *         page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<TldStar>>> getAllTldStar(int page, int rec) {
		BaseResponse<List<TldStar>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all TldStar Data");
			if (page == -1 && rec == -1) {
				List<TldStar> tldstar = tldstarRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, tldstar));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<TldStar> tldstarPages = tldstarRepository.findAll(pages);
			if (tldstarPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						tldstarPages.getContent(), tldstarPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all TldStar data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	/**
	 * Retrieves a specific TldStar data by its ID.
	 *
	 * @param id The ID of the TldStar to retrieve.
	 * @return A ResponseDto containing the TldStar entity with the specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<TldStar>> getTldStarById(Long id) {
		BaseResponse<TldStar> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching TldStar Data By Id");
			Optional<TldStar> tldstar = tldstarRepository.findById(id);
			if (tldstar.isPresent()) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, tldstar.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching TldStar data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}

	@Override
	public ResponseEntity<ResponseDto<List<TldStar>>> searchTldStar(TldStarQuerySearchDto tldstarQuerySearch, int page,
			int rec) {
		BaseResponse<List<TldStar>> responseObj = new BaseResponse<>();
		List<TldStar> searchTldStars = new ArrayList<>();

		try {
			Long total = 0L;
			String sanitizedlastWhere = "";
			String searchQuery = "";
			String sanitizedcountQuery = "";
			Field _queryField = OracleHelpers.getField(tldstarQuerySearch, "lastWhere");
			if (_queryField != null) {
				_queryField.setAccessible(true);
				Object value = _queryField.get(tldstarQuerySearch);
//					sanitizedqueryWhere = (String) value;
				sanitizedlastWhere = app.sanitizeValueCheck((String) value);
			}
			// Total Count Process
			if (OracleHelpers.isNullorEmpty(sanitizedlastWhere)) {
				String countQuery = app.getQuery(tldstarQuerySearch, "tld_star", "",
						"customer_ident,airport_ident,airport_icao,star_ident", true,
						page == -1 || rec == -1 ? true : false);
				Record record = app.selectInto(countQuery);
				total = record.getLong();
				searchQuery = app.getQuery(tldstarQuerySearch, "tld_star", "",
						"customer_ident,airport_ident,airport_icao,star_ident", false,
						page == -1 || rec == -1 ? true : false);
				List<Record> records = null;
				if (page == -1 || rec == -1) {
					records = app.executeQuery(searchQuery);
				} else {
					int offset = page;
					records = app.executeQuery(searchQuery, offset, rec);
				}

				for (Record searchRec : records) {
					searchTldStars.add(app.mapResultSetToClass(searchRec, TldStar.class));
				}
			} else {
				String countQuery = app.getQuery(tldstarQuerySearch, "tld_star", sanitizedlastWhere,
						"customer_ident,airport_ident,airport_icao,star_ident", true,
						page == -1 || rec == -1 ? true : false);
				sanitizedcountQuery = app.sanitizeValueCheck(countQuery);
				Record record = app.selectInto(sanitizedcountQuery);
				total = record.getLong();
				searchQuery = app.getQuery(tldstarQuerySearch, "tld_star", sanitizedlastWhere,
						"customer_ident,airport_ident,airport_icao,star_ident", false,
						page == -1 || rec == -1 ? true : false);
				List<Record> records = null;
				if (page == -1 || rec == -1) {
					records = app.executeQuery(searchQuery);
				} else {
					int offset = page;
					records = app.executeQuery(searchQuery, offset, rec);
				}

				for (Record searchRec : records) {
					searchTldStars.add(app.mapResultSetToClass(searchRec, TldStar.class));
				}
			}
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchTldStars, total,
					hashUtils.encrypt(searchQuery)));
		}catch (RuntimeException e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		} 
		catch (Exception e) {
			return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
		}
	}
}
