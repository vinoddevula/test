package com.honeywell.coreptdu.datatypes.setwhereclause.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.honeywell.coreptdu.datatypes.setwhereclause.block.Control;
import com.honeywell.coreptdu.datatypes.setwhereclause.block.SetWhere;
import com.honeywell.coreptdu.datatypes.setwhereclause.dto.request.SetWhereClauseTriggerRequestDto;
import com.honeywell.coreptdu.datatypes.setwhereclause.dto.response.SetWhereClauseTriggerResponseDto;
import com.honeywell.coreptdu.datatypes.setwhereclause.entity.CptForQueryCriteria;
import com.honeywell.coreptdu.datatypes.setwhereclause.service.ISetWhereClauseTriggerService;
import com.honeywell.coreptdu.exception.AlertException;
import com.honeywell.coreptdu.exception.ExceptionUtils;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.Global;
import com.honeywell.coreptdu.global.dto.Parameter;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.global.dto.SystemDto;
import com.honeywell.coreptdu.global.forms.Event;
import com.honeywell.coreptdu.global.forms.WindowDetail;
import com.honeywell.coreptdu.pkg.body.DisplayAlert;
import com.honeywell.coreptdu.pll.CoreptLib;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.GenericNativeQueryHelper;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Block;
import com.honeywell.coreptdu.utils.oracleutils.GenericTemplateForm;
import com.honeywell.coreptdu.utils.oracleutils.OracleHelpers;
import com.honeywell.coreptdu.utils.oracleutils.PropertyHelpers;
import com.honeywell.coreptdu.utils.oracleutils.Record;
import com.honeywell.coreptdu.utils.oracleutils.RecordGroup;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequestScope
public class SetWhereClauseTriggerServiceImpl extends GenericTemplateForm<SetWhereClauseTriggerServiceImpl>
		implements ISetWhereClauseTriggerService {

	@Getter
	@Setter
	private Block<CptForQueryCriteria> cptForQueryCriteria = new Block<>();
	@Getter
	@Setter
	private Control control = new Control();
	@Getter
	@Setter
	private Global global = new Global();
	// coverity-fixes
	@Getter
	@Setter
	private List<String> blocksOrder = new ArrayList<>();
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
	private Map<String, WindowDetail> windows = new HashMap<>();

	// coverity-fixes
	@Autowired
	@Getter
	@Setter
	private DisplayAlert displayAlert;

	@Getter
	@Setter
	@Autowired
	private IApplication app;

	@Autowired
	private CoreptLib coreptLib;
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
		super.system = this.system;
		super.global = this.global;
		// Coverity-fixes
		super.blocksOrder = this.blocksOrder;
		super.windows = this.windows;
//		super.displayItemBlock = this.displayItemBlock;
		OracleHelpers.bulkClassMapper(this, displayAlert);
		OracleHelpers.bulkClassMapper(this, coreptLib);
		coreptLib.initialization(this);
		// OracleHelpers.bulkClassMapper(this, corepttemplatetriggerserviceimpl);
		// OracleHelpers.bulkClassMapper(this, coreptMenuMmbServiceImpl);
		// corepttemplatetriggerserviceimpl.initialization(this);
		// coreptMenuMmbServiceImpl.initialization(this);
		// OracleHelpers.bulkClassMapper(this, refreshMasterLibrary);
		// refreshMasterLibrary.initialization(this);
	}

	@Override
	public String setOneField(String pFieldName, String pValue) throws Exception {
		log.info("setOneField Executing");
//		String query = "";
//		Record rec = null;
		try {
			String vWhere = null;
			Boolean vMore = false;
			Boolean vLike = false;
			Boolean vCondition = false;
			Boolean vNull = false;
			String vValue = null;
log.debug("Initial value of vWhere:"+vWhere);
			if (pValue == null) {
				vWhere = pFieldName + " is null ";
				return vWhere;

			}

			if (instr(pValue, ",", 1) > 0) {
				vMore = true;

			}

			if (instr(pValue, "%", 1) > 0 || instr(pValue, "_", 1) > 0) {
				vLike = true;

			}

			if (Arrays.asList(">", "<", "=").contains(substr(pValue, 1, 1))
					|| Objects.equals(substr(pValue, 1, 2), "!=")
					|| Arrays.asList("IN ", "IN(", "IS ").contains(substr(upper(pValue), 1, 3))
					|| like("NOT %", upper(pValue)) || like("BETWEEN%AND%", upper(pValue)) || like("LIKE %", pValue)) {
				vCondition = true;

			}

			if (like("NULL%", upper(pValue)) || like("NOT NULL%", upper(pValue))) {
				vNull = true;

			}

			if (Objects.equals(vLike, false)) {
				if (Objects.equals(vCondition, true) || Objects.equals(vNull, true)) {
					if (Objects.equals(vNull, true)) {
						vWhere = pFieldName + " IS " + pValue + " ";

					}

					else {
						vWhere = pFieldName + " " + pValue + " ";

					}

				}

				else {
					if (Objects.equals(vMore, false)) {
						if (like("''%", pValue)) {
							vWhere = pFieldName + " = " + pValue + " ";

						}

						else {
							vWhere = pFieldName + " = '" + pValue + "' ";

						}

					}

					else {
						if (like("''%", pValue)) {
							vWhere = pFieldName + " in (";
							for (int i = 0; i <= length(pValue) - 1; i++) {
								if (Objects.equals(substr(pValue, i, 1), ",")) {
									vWhere = vWhere + ",";

								}

								else {
									vWhere = vWhere.concat(substr(pValue, i, 1));

								}

							}
							if (!Objects.equals(substr(pValue, 1), ",")) {
								vWhere = vWhere + substr(pValue, length(pValue), 1);

							}

							vWhere = vWhere + ") ";

						}

						else {
							vWhere = pFieldName + " in ('";
							for (int i = 0; i <= length(pValue) - 1; i++) {
								if (Objects.equals(substr(pValue, i, 1), ",")) {
									vWhere = vWhere + "','";

								}

								else {
									vWhere = vWhere + substr(pValue, i, 1);

								}

							}
							if (!Objects.equals(substr(pValue, length(pValue), 1), ",")) {
								vWhere = vWhere + substr(pValue, length(pValue), 1);

							}

							vWhere = vWhere + "') ";

						}

					}

				}

			}

			else {
				if (!Objects.equals(length(rtrimWithValue(pValue, "_")), length(pValue))) {
					vValue = rtrimWithValue(pValue, "_");
					vValue = vValue + "%";
					// vValue = rtrim(pValue, "_") + "%";
				}

				else {
					vValue = pValue;

				}
				if (Objects.equals(vMore, false)) {
					if (like("''%", vValue)) {
						vWhere = pFieldName + " like " + vValue + " ";

					}

					else {
						if (like("%like%", lower(vValue))) {
							vWhere = pFieldName + " " + vValue + " ";

						}

						else {
							vWhere = pFieldName + " like '" + vValue + "' ";

						}

					}

				}

				else {
					Integer lPrv = 1;
					Integer lNxt = 0;
					Integer lLen = length(vValue);
					Integer lCm = 0;

					vWhere = "(";
					for (int i = 0; i <= lLen; i++) {
						if (Objects.equals(substr(vValue, i, 1), ",")) {
							lPrv = lNxt + 1;
							lNxt = i;
							lCm = lCm + 1;
							if (Objects.equals(lCm, 1)) {
								if (like("''%", vValue)) {
									vWhere = vWhere + pFieldName + " like " + substr(vValue, lPrv, lNxt - lPrv) + " ";

								}

								else {
									vWhere = vWhere + pFieldName + " like '" + substr(vValue, lPrv, lNxt - lPrv) + "' ";

								}

							}

							else {
								if (like("''%", vValue)) {
									vWhere = vWhere + " or " + pFieldName + " like " + substr(vValue, lPrv, lNxt - lPrv)
											+ " ";

								}

								else {
									vWhere = vWhere + " or " + pFieldName + " like '"
											+ substr(vValue, lPrv, lNxt - lPrv) + "' ";

								}

							}

						}

					}
					lPrv = lNxt + 1;
					lNxt = lLen + 1;
					if (like("''%", vValue)) {
						vWhere = vWhere + " or " + pFieldName + " like " + substr(vValue, lPrv, lNxt - lPrv) + ") ";

					}

					else {
						vWhere = vWhere + " or " + pFieldName + " like '" + substr(vValue, lPrv, lNxt - lPrv) + "') ";

					}

				}

			}

			log.info("setOneField Executed Successfully");
			return vWhere;

		} catch (Exception e) {
			log.error("Error while executing setOneField" + e.getMessage());
			throw e;

		}
	}

	@Override
	public SetWhere setWhere(String vWhere1, String vWhere2) throws Exception {
		log.info("setWhere Executing");
//		String query = "";
//		Record rec = null;
		SetWhere dto = new SetWhere();
		try {
			Integer vNfld = 0;
			String vSelcrit = null;

			goBlock("cptForQueryCriteria", "");
			firstRecord("cptForQueryCriteria");
			for (int i = 0; i <= cptForQueryCriteria.size() - 1; i++) {
				if (!Objects.equals(cptForQueryCriteria.getRow(i).getSelectionValues(), null)) {
					vSelcrit = setOneField(cptForQueryCriteria.getRow(i).getColumnName(),
							ltrim(rtrim(cptForQueryCriteria.getRow(i).getSelectionValues())));
					if (!Objects.equals(vSelcrit, null)) {
						vNfld = vNfld + 1;
						if (Arrays
								.asList("APCH_ROUTE_QUALIFIER_1", "APCH_ROUTE_QUALIFIER_2", "QUALIFIER_1",
										"QUALIFIER_2", "ALT_LEVEL", "ROUTE_TYPE")
								.contains(cptForQueryCriteria.getRow(i).getColumnName())
								|| Objects.equals(cptForQueryCriteria.getRow(i).getColumnDescription(),
										"Segment Cycle Indicator")
								|| Objects.equals(cptForQueryCriteria.getRow(i).getColumnDescription(),
										"Segment Create DCR Number")) {
							if (Objects.equals(dto.getVWhere2(), null)) {
								dto.setVWhere2(vSelcrit);

							}

							else {
								dto.setVWhere2(dto.getVWhere2() + " and " + vSelcrit);

							}

						}

						else {
							if (Objects.equals(dto.getVWhere1(), null)) {
								dto.setVWhere1(vSelcrit);

							}

							else {
								dto.setVWhere1(dto.getVWhere1() + " and " + vSelcrit);

							}

						}

					}

				}

				if (Objects.equals(system.getLastRecord(), true)) {

					firstRecord(system.getCursorBlock());
					break;

				}

				else {
					nextRecord("");

				}

			}
			log.info("setWhere Executed Successfully");
			return dto;
		} catch (Exception e) {
			log.error("Error while executing setWhere" + e.getMessage());
			throw e;

		}
	}

	@Override
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> keyEntqry(
			SetWhereClauseTriggerRequestDto reqDto) throws Exception {
		log.info(" keyEntqry Executing");
		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
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
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> keyExeqry(
			SetWhereClauseTriggerRequestDto reqDto) throws Exception {
		log.info(" keyExeqry Executing");
		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
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
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> whenNewFormInstance(
			SetWhereClauseTriggerRequestDto reqDto) throws Exception {
		log.info(" whenNewFormInstance Executing");
		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			String vWhere = null;
			global.setGWhere(null);
			defaultValue("J", "global.dataSupplier");
			// defaultValue("200212", "global.data_supplier");
			defaultValue("maximize", "global.statusWindow");
			// defaultValue("procedure", "global.vpTable");
			if (Arrays.asList("companyRoute", "enrouteAirway").contains(global.getVpTable())) {
				vWhere = "table_name = upper('" + toSnakeCase(global.getVpTable()) + "') ";
			} else if (like("airportS%", global.getVpTable()) || like("heliportS%", global.getVpTable())) {
				vWhere = "table_name = 'PROCEDURE' ";

			}

			else {
				vWhere = "table_name = 'APPROACH' ";

			}
			if (Objects.equals(global.getLibraryAccess(), "PRE-LIBRARY")) {
				vWhere = vWhere + " and nvl(mst_lib_only_flag,'N') = 'N' ";
			} else if (Objects.equals(global.getLibraryAccess(), "MASTER")) {
				vWhere = vWhere + " and nvl(pre_lib_only_flag,'N') = 'N' ";

			} else {

			}
			if (Objects.equals(global.getRecordType(), "T")) {
				vWhere = vWhere + " and nvl(tld_only_flag,'Y') = 'Y' ";

			} else {
				vWhere = vWhere + " and nvl(tld_only_flag,'N') = 'N' ";
			}
			if (like("%companyRoute%", global.getVpTable()) || like("%enrouteAirway%", global.getVpTable())) {
				vWhere = vWhere + " and DISPLAY_ORDER >= 1 ";
			}

			else if (like("%APPROACH%", global.getVpTable())) {
				vWhere = vWhere + " and DISPLAY_ORDER >= -1  ORDER BY DISPLAY_ORDER";

			}

			else if (like("%STAR%", global.getVpTable()) || like("%SID%", global.getVpTable())) {
				vWhere = vWhere + " and DISPLAY_ORDER >= 1 or DISPLAY_ORDER in(-2,-3)";

			}

			setBlockProperty("cptForQueryCriteria", "default_where", vWhere);
			system.setCursorBlock("cptForQueryCriteria");
			String query = "SELECT TABLE_NAME, COLUMN_NAME, PRE_LIB_ONLY_FLAG, MST_LIB_ONLY_FLAG, TLD_ONLY_FLAG, DISPLAY_ORDER,\r\n"
					+ "COLUMN_DESCRIPTION FROM CPTS.cpt_for_query_criteria WHERE " + vWhere;
			String queryHits = "SELECT COUNT(*) FROM (" + query + ")";
			List<Record> rec = app.executeQuery(query);
			for (int i = 0; i <= rec.size() - 1; i++) {
				cptForQueryCriteria.getRow(i).setTableName(rec.get(i).getString());
				cptForQueryCriteria.getRow(i).setColumnName(rec.get(i).getString());
				cptForQueryCriteria.getRow(i).setPreLibOnlyFlag(rec.get(i).getString());
				cptForQueryCriteria.getRow(i).setMstLibOnlyFlag(rec.get(i).getString());
				cptForQueryCriteria.getRow(i).setTldOnlyFlag(rec.get(i).getString());
				cptForQueryCriteria.getRow(i).setDisplayOrder(rec.get(i).getInt());
				cptForQueryCriteria.getRow(i).setColumnDescription(rec.get(i).getString());
				cptForQueryCriteria.getRow(i).setRecordStatus("QUERIED");
				cptForQueryCriteria.add(new CptForQueryCriteria());

			}
			Record op = app.selectInto(queryHits);
			cptForQueryCriteria.setQueryHits(toString(op.getInt()));
			if (!Objects.equals(toInteger(cptForQueryCriteria.getQueryHits()), 0)) {
//				coreptLib.unsetQueryMenuItems(); //MULTI-WINDOW FIX
				;
			}
			// cptForQueryCriteria.remove(op.getInt());
//			executeQuery(this, system.getCursorBlock(), vWhere,null,
//                null);
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" whenNewFormInstance executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the whenNewFormInstance Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

//	@Override
//	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> whenTimerExpired(SetWhereClauseTriggerRequestDto reqDto) throws Exception{
//		log.info(" whenTimerExpired Executing");
//		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
//		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
//		try{
//		OracleHelpers.bulkClassMapper(reqDto, this);
//		String curBlk = system.getCursorBlock();
//		Integer vButton = 0;
//
//		if(!idNull(findTimer("accept_alarm"))) {
//
//		//TODO delete_timer("accept_alarm");
//		if(Objects.equals(displayAlert.more_buttons("S","Check Query","Check the query. Then select an option: ","Accept","Modify Query"), 1)) {
//
//		//TODO pc3_do_key("exit");
//
//		}
//
//		
//
//		}
//
//		
//		OracleHelpers.ResponseMapper(this, resDto);
//		log.info(" whenTimerExpired executed successfully");
//			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,resDto));
//		}
//		catch(Exception e) {
//			log.error("Error while Executing the whenTimerExpired Service");
//			OracleHelpers.ResponseMapper(this, resDto);
//			return ExceptionUtils.handleException(e, resDto);		}
//	}
	@Override
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> keyExit(SetWhereClauseTriggerRequestDto reqDto)
			throws Exception {
		log.info(" keyExit Executing");
		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			exitForm();
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
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> controlCancelWhenButtonPressed(
			SetWhereClauseTriggerRequestDto reqDto) throws Exception {
		log.info(" controlCancelWhenButtonPressed Executing");
		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
			global.setGWhere(null);
			doKey("exit");
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlCancelWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlCancelWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> controlAcceptWhenButtonPressed(
			SetWhereClauseTriggerRequestDto reqDto) throws Exception {
		log.info(" controlAcceptWhenButtonPressed Executing");
		BaseResponse<SetWhereClauseTriggerResponseDto> responseObj = new BaseResponse<>();
		SetWhereClauseTriggerResponseDto resDto = new SetWhereClauseTriggerResponseDto();
		try {
			OracleHelpers.bulkClassMapper(reqDto, this);
//			final Integer cstMs = 50;
//			Object vTimerId = null;
			String vWhere = null;
			String vWhereSeg = null;
			SetWhere dto = setWhere(vWhere, vWhereSeg);
			vWhere = dto.getVWhere1();
			vWhereSeg = dto.getVWhere2();
			Integer vButton;

			if (!Objects.equals(vWhere, null) && !Objects.equals(vWhereSeg, null)) {
				control.setMyWhere("where " + vWhereSeg + " and " + vWhere + " and data_supplier = '"
						+ global.getDataSupplier() + "' ");

			} else {
				if (!Objects.equals(vWhere, null)) {
					control.setMyWhere("where " + vWhere + " and data_supplier = '" + global.getDataSupplier() + "' ");

				}

				else if (!Objects.equals(vWhereSeg, null)) {
					control.setMyWhere(
							"where " + vWhereSeg + " and data_supplier = '" + global.getDataSupplier() + "' ");

				}

			}
			if (!Objects.equals(vWhere, null)) {
				global.setGWhere(vWhere + " and data_supplier = '" + global.getDataSupplier() + "' ");

			}

			if (!Objects.equals(vWhereSeg, null)) {
				global.setGWhereSeg(vWhereSeg + " and data_supplier = '" + global.getDataSupplier() + "' ");

			}

			alertDetails.getCurrent();
			if (alertDetails.getAlerts().size() < alertDetails.getCurrentAlert()) {

				displayAlert.moreButtons("C", "Check Query", "Check the query. Then select an option: ", "Accept",
						"Modify Query", null);
				OracleHelpers.ResponseMapper(displayAlert, this);
				alertDetails.createNewRecord("Check Query");
				throw new AlertException(event, alertDetails);
			} else {
				vButton = alertDetails.getAlertValue("Check Query", alertDetails.getCurrentAlert());
			}
			if (Objects.equals(vButton, 1)) {
				coreptLib.setadditionalwhere(global.getVpTable());
				doKey("exit");
			} else if (Objects.equals(vButton, 2)) {
				goBlock("cptForQueryCriteria", "");
				firstRecord("cptForQueryCriteria");
			}

			// Timer yet to be implement
			OracleHelpers.ResponseMapper(this, resDto);
			log.info(" controlAcceptWhenButtonPressed executed successfully");
			return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, resDto));
		} catch (Exception e) {
			log.error("Error while Executing the controlAcceptWhenButtonPressed Service");
			OracleHelpers.ResponseMapper(this, resDto);
			return ExceptionUtils.handleException(e, resDto);
		}
	}

	@Override
	public ResponseEntity<ResponseDto<SetWhereClauseTriggerResponseDto>> whenTimerExpired(
			SetWhereClauseTriggerRequestDto reqDto) throws Exception {
		return null;
	}

}
--------------------------------
package com.honeywell.coreptdu.datatypes.setwhereclause.serviceimpl;

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

import com.honeywell.coreptdu.datatypes.setwhereclause.dto.request.CptForQueryCriteriaQuerySearchDto;
import com.honeywell.coreptdu.datatypes.setwhereclause.dto.request.CptForQueryCriteriaRequestDto;
import com.honeywell.coreptdu.datatypes.setwhereclause.entity.CptForQueryCriteria;
import com.honeywell.coreptdu.datatypes.setwhereclause.repository.ICptForQueryCriteriaRepository;
import com.honeywell.coreptdu.datatypes.setwhereclause.service.ICptForQueryCriteriaService;
import com.honeywell.coreptdu.global.dto.BaseResponse;
import com.honeywell.coreptdu.global.dto.ResponseDto;
import com.honeywell.coreptdu.utils.common.Constants;
import com.honeywell.coreptdu.utils.dbutils.IApplication;
import com.honeywell.coreptdu.utils.oracleutils.Record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * CptForQueryCriteria Service Implementation.
 */
@Slf4j
@Service
@RequestScope
public class CptForQueryCriteriaServiceImpl implements ICptForQueryCriteriaService {

	@Autowired
	ICptForQueryCriteriaRepository cptforquerycriteriaRepository;

	@Autowired
	private IApplication app;

	/**
	 * Retrieves a list of CptForQueryCriteria with optional pagination.
	 *
	 * @param page The page number for pagination (optional).
	 * @param rec  The number of records per page for pagination (optional).
	 * @return A ResponseDto containing the list of CptForQueryCriteria based on the
	 *         specified page and rec parameters.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<CptForQueryCriteria>>> getAllCptForQueryCriteria(int page, int rec) {
		BaseResponse<List<CptForQueryCriteria>> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching all CptForQueryCriteria Data");
			if (page == -1 && rec == -1) {
				List<CptForQueryCriteria> cptforquerycriteria = cptforquerycriteriaRepository.findAll();
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, cptforquerycriteria));
			}
			Pageable pages = PageRequest.of(page, rec);
			Page<CptForQueryCriteria> cptforquerycriteriaPages = cptforquerycriteriaRepository.findAll(pages);
			if (cptforquerycriteriaPages.getContent().size() > 0) {
				return responseObj.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH,
						cptforquerycriteriaPages.getContent(), cptforquerycriteriaPages.getTotalElements()));
			} else {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_NOT_FOUND_MESSAGE, List.of()));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching all CptForQueryCriteria data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
		}
	}

	/**
	 * Retrieves a specific CptForQueryCriteria data by its ID.
	 *
	 * @param id The ID of the CptForQueryCriteria to retrieve.
	 * @return A ResponseDto containing the CptForQueryCriteria entity with the
	 *         specified ID.
	 */
	@Override
	public ResponseEntity<ResponseDto<CptForQueryCriteria>> getCptForQueryCriteriaById(Long id) {
		BaseResponse<CptForQueryCriteria> responseObj = new BaseResponse<>();
		try {
			log.info("Fetching CptForQueryCriteria Data By Id");
			Optional<CptForQueryCriteria> cptforquerycriteria = cptforquerycriteriaRepository.findById(id);
			if (cptforquerycriteria.isPresent()) {
				return responseObj
						.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, cptforquerycriteria.get()));
			} else {
				return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
			}
		} catch (Exception ex) {
			log.error("An error occurred while fetching CptForQueryCriteria data by Id", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
		}
	}

	/**
	 * Creates new CptForQueryCriterias based on the provided list of DTOs.
	 *
	 * @param createcptforquerycriterias The list of DTOs containing data for
	 *                                   creating CptForQueryCriteria.
	 * @return A ResponseDto containing the list of created CptForQueryCriteria
	 *         entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<CptForQueryCriteria>>> createCptForQueryCriteria(
			List<CptForQueryCriteriaRequestDto> cptforquerycriteriasCreate) {
		BaseResponse<List<CptForQueryCriteria>> responseObj = new BaseResponse<>();
		List<CptForQueryCriteria> createdCptForQueryCriterias = new ArrayList<>();

		for (CptForQueryCriteriaRequestDto cptforquerycriteriaCreate : cptforquerycriteriasCreate) {
			try {
				log.info("Creating CptForQueryCriteria Data");
				CptForQueryCriteria cptforquerycriteria = new CptForQueryCriteria();
				cptforquerycriteria.setTableName(cptforquerycriteriaCreate.getTableName());
				cptforquerycriteria.setColumnName(cptforquerycriteriaCreate.getColumnName());
				cptforquerycriteria.setColumnDescription(cptforquerycriteriaCreate.getColumnDescription());
				cptforquerycriteria.setPreLibOnlyFlag(cptforquerycriteriaCreate.getPreLibOnlyFlag());
				cptforquerycriteria.setMstLibOnlyFlag(cptforquerycriteriaCreate.getMstLibOnlyFlag());
				cptforquerycriteria.setTldOnlyFlag(cptforquerycriteriaCreate.getTldOnlyFlag());
				cptforquerycriteria.setDisplayOrder(cptforquerycriteriaCreate.getDisplayOrder());
				cptforquerycriteria.setTableName(cptforquerycriteriaCreate.getTableName());
				cptforquerycriteria.setDisplayOrder(cptforquerycriteriaCreate.getDisplayOrder());
				cptforquerycriteria.setColumnName(cptforquerycriteriaCreate.getColumnName());
				cptforquerycriteria.setTldOnlyFlag(cptforquerycriteriaCreate.getTldOnlyFlag());
				cptforquerycriteria.setMstLibOnlyFlag(cptforquerycriteriaCreate.getMstLibOnlyFlag());
				cptforquerycriteria.setPreLibOnlyFlag(cptforquerycriteriaCreate.getPreLibOnlyFlag());
				cptforquerycriteria.setColumnDescription(cptforquerycriteriaCreate.getColumnDescription());
				CptForQueryCriteria createdCptForQueryCriteria = cptforquerycriteriaRepository
						.save(cptforquerycriteria);
				createdCptForQueryCriterias.add(createdCptForQueryCriteria);
			} catch (Exception ex) {
				log.error("An error occurred while creating CptForQueryCriteria data", ex.getMessage());
				return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
			}
		}
		return responseObj
				.render(responseObj.formSuccessResponse(Constants.RECORD_CREATED, createdCptForQueryCriterias));
	}

	/**
	 * Updates existing CptForQueryCriterias based on the provided list of DTOs.
	 *
	 * @param cptforquerycriteriasUpdate The list of DTOs containing data for
	 *                                   updating CptForQueryCriteria.
	 * @return A ResponseDto containing the list of updated CptForQueryCriteria
	 *         entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<CptForQueryCriteria>>> updateCptForQueryCriteria(
			List<CptForQueryCriteriaRequestDto> cptforquerycriteriasUpdate) {
		BaseResponse<List<CptForQueryCriteria>> responseObj = new BaseResponse<>();
		List<CptForQueryCriteria> updatedCptForQueryCriterias = new ArrayList<>();

//		for (CptForQueryCriteriaRequestDto cptforquerycriteriaUpdate : cptforquerycriteriasUpdate) {
//			try {
//				log.info("Updating CptForQueryCriteria Data");
//				if (existingCptForQueryCriteriaOptional.isPresent()) {
//					CptForQueryCriteria existingCptForQueryCriteria = existingCptForQueryCriteriaOptional.get();
//						existingCptForQueryCriteria.setTableName(cptforquerycriteriaUpdate.getTableName());
//						existingCptForQueryCriteria.setColumnName(cptforquerycriteriaUpdate.getColumnName());
//						existingCptForQueryCriteria.setColumnDescription(cptforquerycriteriaUpdate.getColumnDescription());
//						existingCptForQueryCriteria.setPreLibOnlyFlag(cptforquerycriteriaUpdate.getPreLibOnlyFlag());
//						existingCptForQueryCriteria.setMstLibOnlyFlag(cptforquerycriteriaUpdate.getMstLibOnlyFlag());
//						existingCptForQueryCriteria.setTldOnlyFlag(cptforquerycriteriaUpdate.getTldOnlyFlag());
//						existingCptForQueryCriteria.setDisplayOrder(cptforquerycriteriaUpdate.getDisplayOrder());
//						existingCptForQueryCriteria.setTableName(cptforquerycriteriaUpdate.getTableName());
//						existingCptForQueryCriteria.setDisplayOrder(cptforquerycriteriaUpdate.getDisplayOrder());
//						existingCptForQueryCriteria.setColumnName(cptforquerycriteriaUpdate.getColumnName());
//						existingCptForQueryCriteria.setTldOnlyFlag(cptforquerycriteriaUpdate.getTldOnlyFlag());
//						existingCptForQueryCriteria.setMstLibOnlyFlag(cptforquerycriteriaUpdate.getMstLibOnlyFlag());
//						existingCptForQueryCriteria.setPreLibOnlyFlag(cptforquerycriteriaUpdate.getPreLibOnlyFlag());
//						existingCptForQueryCriteria.setColumnDescription(cptforquerycriteriaUpdate.getColumnDescription());
//					CptForQueryCriteria updatedCptForQueryCriteria = cptforquerycriteriaRepository.save(existingCptForQueryCriteria);
//					updatedCptForQueryCriterias.add(updatedCptForQueryCriteria);
//				} else {
//					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
//				}
//			} catch (Exception ex) {
//				log.error("An error occurred while updating CptForQueryCriteria data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.UPDATE_FAILED));
//			}
//		}
		return responseObj
				.render(responseObj.formSuccessResponse(Constants.UPDATE_SUCCESS, updatedCptForQueryCriterias));
	}

	/**
	 * Deletes existing CptForQueryCriterias based on the provided list of DTOs.
	 *
	 * @param deletecptforquerycriterias The list of DTOs containing data for
	 *                                   deleting CptForQueryCriteria.
	 * @return A ResponseDto containing the list of deleted CptForQueryCriteria
	 *         entities.
	 */
	@Override
	public ResponseEntity<ResponseDto<List<CptForQueryCriteria>>> deleteCptForQueryCriteria(
			List<CptForQueryCriteriaRequestDto> cptforquerycriteriaDeletes) {
		BaseResponse<List<CptForQueryCriteria>> responseObj = new BaseResponse<>();
		List<CptForQueryCriteria> deletedCptForQueryCriterias = new ArrayList<>();

//		for (CptForQueryCriteriaRequestDto cptforquerycriteriaDelete : cptforquerycriteriaDeletes) {
//			try {
//				log.info("Deleting CptForQueryCriteria Data");
//				Optional<CptForQueryCriteria> existingCptForQueryCriteriaOptional = cptforquerycriteriaRepository.findById(CptForQueryCriteriaId);
//				if (existingCptForQueryCriteriaOptional.isPresent()) {
//					CptForQueryCriteria existingCptForQueryCriteria = existingCptForQueryCriteriaOptional.get();
//					cptforquerycriteriaRepository.deleteById(existingCptForQueryCriteria.getId());
//					deletedCptForQueryCriterias.add(existingCptForQueryCriteria);
//				} else {
//					return responseObj.render(responseObj.formErrorResponse(Constants.RECORD_NOT_FOUND_MESSAGE));
//				}
//			} catch (Exception ex) {
//				log.error("An error occurred while deleting CptForQueryCriteria data", ex.getMessage());
//				return responseObj.render(responseObj.formErrorResponse(Constants.DELETE_FAILED));
//			}
//		}
		return responseObj
				.render(responseObj.formSuccessResponse(Constants.DELETE_MESSAGE, deletedCptForQueryCriterias));
	}

	@Override
	public ResponseEntity<ResponseDto<List<CptForQueryCriteria>>> searchCptForQueryCriteria(
			CptForQueryCriteriaQuerySearchDto cptforquerycriteriaQuerySearch, int page, int rec) {
		BaseResponse<List<CptForQueryCriteria>> responseObj = new BaseResponse<>();
		List<CptForQueryCriteria> searchCptForQueryCriterias = new ArrayList<>();

		try {
			Long total = 0L;
			// Total Count Process
			String countQuery = app.getQuery(cptforquerycriteriaQuerySearch, "cpt_for_query_criteria", "",
					"display_order", true, page == -1 || rec == -1 ? true : false);
			Record record = app.selectInto(countQuery);
			total = record.getLong();
			String searchQuery = app.getQuery(cptforquerycriteriaQuerySearch, "cpt_for_query_criteria", "",
					"display_order", false, page == -1 || rec == -1 ? true : false);
			List<Record> records = null;
			if (page == -1 || rec == -1) {
				records = app.executeQuery(searchQuery);
			} else {
				records = app.executeQuery(searchQuery, page, rec);
			}

			for (Record searchRec : records) {
				searchCptForQueryCriterias.add(app.mapResultSetToClass(searchRec, CptForQueryCriteria.class));
			}
			return responseObj
					.render(responseObj.formSuccessResponse(Constants.RECORD_FETCH, searchCptForQueryCriterias, total));
		} catch (Exception ex) {
			log.error("An error occurred while querying the data", ex.getMessage());
			return responseObj.render(responseObj.formErrorResponse(ex.getMessage()));
		}
	}
}
