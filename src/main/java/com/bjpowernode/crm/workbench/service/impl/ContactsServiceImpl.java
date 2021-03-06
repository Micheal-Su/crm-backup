package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.settings.dao.UserDao;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.utils.SqlSessionUtil;
import com.bjpowernode.crm.utils.UUIDUtil;
import com.bjpowernode.crm.vo.PaginationVO;
import com.bjpowernode.crm.workbench.dao.*;
import com.bjpowernode.crm.workbench.dao.ContactsDao;
import com.bjpowernode.crm.workbench.domain.*;
import com.bjpowernode.crm.workbench.service.ContactsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContactsServiceImpl implements ContactsService {
    private ContactsDao contactsDao = null;
    private ContactsActivityRelationDao contactsActivityRelationDao = SqlSessionUtil.getSqlSession().getMapper(ContactsActivityRelationDao.class);
    private ContactsCustomerRelationDao contactsCustomerRelationDao = SqlSessionUtil.getSqlSession().getMapper(ContactsCustomerRelationDao.class);
    private ContactsRemarkDao contactsRemarkDao = SqlSessionUtil.getSqlSession().getMapper(ContactsRemarkDao.class);;
    private UserDao userDao = SqlSessionUtil.getSqlSession().getMapper(UserDao.class);
    private CustomerDao customerDao = SqlSessionUtil.getSqlSession().getMapper(CustomerDao.class);
    private TranDao tranDao = SqlSessionUtil.getSqlSession().getMapper(TranDao.class);

    @Override
    public boolean save(Contacts contacts, String customerName) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        boolean flag = true;
        Customer cus = customerDao.getCustomerByName(customerName);
        if (cus==null&&customerName!=""){
            cus = new Customer();
            cus.setId(UUIDUtil.getUUID());
            cus.setName(customerName);
            cus.setCreateBy(contacts.getCreateBy());
            cus.setCreateTime(contacts.getCreateTime());
            cus.setContactSummary(contacts.getContactSummary());
            cus.setNextContactTime(contacts.getNextContactTime());
            cus.setOwner(contacts.getOwner());
            int count1 = customerDao.save(cus);
            if (count1!=1){
                flag=false;
            }
        }
        contacts.setCustomerId(cus.getId());
        int count2 = contactsDao.save(contacts);
        if (count2!=1){
            flag=false;
        }
        ContactsCustomerRelation car = new ContactsCustomerRelation();
        car.setId(UUIDUtil.getUUID());
        car.setContactsId(contacts.getId());
        car.setCustomerId(cus.getId());

        int flag3 = contactsCustomerRelationDao.save(car);
        if(flag3!=1){
            flag=false;
        }
        return flag;
    }

    @Override
    public PaginationVO<Contacts> pageList(Map<String, Object> map) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        int total = contactsDao.getTotalByCondition(map);
        List<Contacts> dataList = contactsDao.getContactsListByCondition(map);
//        List<String> customerNamesList
        //????????????vo????????????total???dataList?????????vo???
        PaginationVO<Contacts> vo = new PaginationVO<>();
        vo.setTotal(total);
        vo.setDataList(dataList);
        //???vo??????
        return vo;
    }

    @Override
    public Contacts detail(String id) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        return contactsDao.detail(id);
    }

    @Override
    public Map<String, Object> getUserListAndContacts(String id) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        List<User> userList = userDao.getUserList();
//        ?????? getById?????????owner???owner???id  detail????????????owner???name??????pagelist???val??????id?????????name
        Contacts c = contactsDao.getById(id);
        Contacts c1 = contactsDao.detail(id);
        String customerName = c1.getCustomerName();
        c.setCustomerName(customerName);
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("contactsUserList",userList);
//      ???????????????map?????????"customerName"?????????Controller?????????printJsonObj??????????????????String???
        map.put("c",c);
        //?????????map????????????
        return map;
    }

    public boolean delete(String[] ids) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        boolean flag = true;
//      ids:??????????????????id

        //???????????????????????????????????????
        int count1 = contactsRemarkDao.getCountByCids(ids);

        //?????????????????????????????????????????????????????????????????????
        int count2 = contactsRemarkDao.deleteByCids(ids);

        if (count1!=count2){
            flag = false;
        }

//        ????????????
        //???????????????????????????????????????
        int count3 = tranDao.getCountByContactsIds(ids);

        //?????????????????????????????????????????????????????????????????????
        int count4 = tranDao.deleteByContactsIds(ids);

        if (count3!=count4){
            flag = false;
        }

        //      ??????????????????????????????
        int count5 = contactsActivityRelationDao.getCountByCids(ids);
        int count6 = contactsActivityRelationDao.unbundByCids(ids);
        if (count5!=count6){
            flag = false;
        }

//        ?????????????????????
        int count7 = contactsDao.deletes(ids);
        return flag;



    }

    @Override
    public List<Tran> getTranListByContactsId(String contactsId) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
         List<Tran> tranList = contactsDao.getTranListByCid(contactsId);
        return tranList;
    }

    @Override
    public List<Contacts> getContactsListByName(String cname) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        return contactsDao.getContactsListByName(cname);
    }

    @Override
    public Contacts getContactsById(String contactsId) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        return contactsDao.getById(contactsId);
    }

    @Override
    public boolean bundActivity(String cid, String[] aids) {
        boolean flag = true;
        for (String aid:aids){
            ContactsActivityRelation car = new ContactsActivityRelation();
            car.setId(UUIDUtil.getUUID());
            car.setContactsId(cid);
            car.setActivityId(aid);
            int count = contactsActivityRelationDao.bund(car);
            if (count != 1){
                flag=false;
            }
        }
        return flag;
    }

    @Override
    public boolean unbundActivity(String id) {
        boolean flag = true;
        int count = contactsActivityRelationDao.unbund(id);
        if (count!=1){
            flag=false;
        }
        return flag;
    }

    @Override
    public boolean deleteInDetail(String id) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        boolean flag = true;

        //???????????????????????????????????????
        int count1 = contactsRemarkDao.getCountByCid(id);

        //?????????????????????????????????????????????????????????????????????
        int count2 = contactsRemarkDao.deleteByCid(id);

        if (count1!=count2){
            flag = false;
        }

        //???????????????????????????????????????
        int count3 = tranDao.getCountByContactsId(id);

        //?????????????????????????????????????????????????????????????????????
        int count4 = tranDao.deleteByContactsId(id);

        if (count3!=count4){
            flag = false;
        }

        //      ??????????????????????????????
        int count5 = contactsActivityRelationDao.getCountByCid(id);
        int count6 = contactsActivityRelationDao.unbundByCid(id);
        if (count5!=count6){
            flag = false;
        }

//        ?????????????????????
        int count7 = contactsDao.deleteById(id);
        if (count7!=1){
            flag=false;
        }
        return flag;

    }

    @Override
    public boolean saveRemark(ContactsRemark cr) {
        boolean flag = true;
        int count = contactsRemarkDao.save(cr);
        if (count!=1){
            flag=false;
        }
        return flag;
    }

    @Override
    public boolean deleteRemark(String id) {
        boolean flag = true;
        int count = contactsRemarkDao.deleteRemark(id);
        if (count!=1){
            flag=false;
        }
        return flag;
    }

    @Override
    public boolean updateRemark(ContactsRemark cr) {
        boolean flag = true;
        int count = contactsRemarkDao.updateRemark(cr);
        if (count != 1){
            flag=false;
        }
        return flag;
    }

    @Override
    public List<ContactsRemark> getRemarkListByCid(String contactsId) {
        List<ContactsRemark> crList = contactsRemarkDao.getListByContactsId(contactsId);
        return crList;
    }


    @Override
    public boolean update(Contacts c, String customerName) {
        contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
        //????????????customerId ,??????????????????????????????????????????????????????????????????????????????
        boolean flag = true;
        Customer cus = customerDao.getCustomerByName(customerName);
        if (cus==null&&customerName!=""){
            cus = new Customer();
            cus.setId(UUIDUtil.getUUID());
            cus.setName(customerName);
            cus.setCreateBy(c.getCreateBy());
            cus.setCreateTime(c.getCreateTime());
            cus.setContactSummary(c.getContactSummary());
            cus.setNextContactTime(c.getNextContactTime());
            cus.setOwner(c.getOwner());
            //????????????
            int count1 = customerDao.save(cus);
            if (count1!=1){
                flag=false;
            }
        }
        //??????????????????
        //????????????id?????????t
        String customerId;
        if (customerName==""){
            customerId = "";
        }else {
            customerId = cus.getId();
        }
        c.setCustomerId(customerId);
        //????????????
        int count2 = contactsDao.update(c);
        if (count2!=1){
            flag=false;
        }
        ContactsCustomerRelation ccr = contactsCustomerRelationDao.getByContactsId(c.getId());
        ccr.setCustomerId(customerId);
        int flag3 = contactsCustomerRelationDao.updateCustomer(ccr);
        if(flag3!=1){
            flag=false;
        }

        return flag;
    }
}
