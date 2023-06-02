import { PageContainer } from "@ant-design/pro-components";
import React, {useEffect, useState} from 'react';
import {List, message} from "antd";
import {
  listInterfaceInfoByPageUsingGET
} from "@/services/yaoapi-backend/interfaceInfoController";
const Index : React.FC = () => {
  const [loading,setLoading] = useState(false)
  const [list,setlist] = useState<API.InterfaceInfo[]>([]);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [total,setTotal] = useState<number>(0);


  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const loadData = async (current= 1, pageSize: number = 8) => {
    setLoading(true);
    try {
      const res = await listInterfaceInfoByPageUsingGET(
        {}
      );
      setlist(res?.data?.records ?? []);
      setTotal(res?.data?.total ?? 0);
    } catch (error: any) {
      message.error('请求失败' + error.message)
    }
    setLoading(false);
  }
   useEffect( () =>{
     loadData();
   },[])

  return (
    <PageContainer title="在线接口开发平台">
      <List
        className="my-list"
        loading={loading}
        itemLayout="horizontal"
        dataSource={list}
        renderItem={(item) => {
          const apiLink = '/interface_info/'+item.id;
          return (
          <List.Item
            actions={[<a key={item.id} href={apiLink}>查看</a>]}
          >
              <List.Item.Meta
                title={<a href={apiLink}>{item.name}</a>}
                description={item.description}
              />
          </List.Item>
        )}
      }
        pagination={{
           showTotal(total:number) {
             return "总数：" + total
           },
            pageSize : 8,
            onChange(page,pageSize){
              loadData(page,pageSize)
            }

          }
        }
      />
    </PageContainer>
  );
};

export default Index;
