package reciter.service.converter;

import reciter.database.model.Identity;
import reciter.service.bean.IdentityBean;

public class IdentityConverter {

	public static IdentityBean convertToDTO(Identity identity) {
		IdentityBean identityDTO = new IdentityBean();
		identityDTO.setIdentityPk(identity.getIdentityPk());
		identityDTO.setCwid(identity.getCwid());
		identityDTO.setStatus(identity.getStatus());
		identityDTO.setOfaPersonPk(identity.getOfaPersonPk());
		identityDTO.setLastName(identity.getLastName());
		identityDTO.setFirstName(identity.getFirstName());
		identityDTO.setFirstInitial(identity.getFirstInitial());
		identityDTO.setMiddleName(identity.getMiddleName());
		identityDTO.setMiddleInitial(identity.getMiddleInitial());
		identityDTO.setFullPublishedName(identity.getFullPublishedName());
		identityDTO.setPrefix(identity.getPrefix());
		identityDTO.setSuffix(identity.getSuffix());
		identityDTO.setTitle(identity.getTitle());
		identityDTO.setAppointmentTypes(identity.getAppointmentTypes());
		identityDTO.setAppointmentPeriod(identity.getAppointmentPeriod());
		identityDTO.setPrimaryDepartment(identity.getPrimaryDepartment());
		identityDTO.setOtherDepartment(identity.getOtherDepartment());
		identityDTO.setPrimaryAffiliation(identity.getPrimaryAffiliation());
		identityDTO.setHarvesterFlag(identity.getHarvesterFlag());
		identityDTO.setCreateDate(identity.getCreateDate());
		identityDTO.setUpdateDate(identity.getUpdateDate());
		identityDTO.setEmail(identity.getEmail());
		identityDTO.setEmailOther(identity.getEmailOther());
		
		return identityDTO;
	}
}
