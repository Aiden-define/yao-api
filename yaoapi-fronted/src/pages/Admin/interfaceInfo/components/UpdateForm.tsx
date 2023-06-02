import { ProColumns, ProTable } from '@ant-design/pro-components';
import { Modal } from 'antd';
import {useEffect, useRef} from "react";

export type Props = {
  columns: ProColumns<API.InterfaceInfo>[];
  onCancel: () => void;
  onSubmit: (values: API.InterfaceInfo) => Promise<void>;
  visible: boolean;
  values: API.InterfaceInfo;
};

const updateForm: React.FC<Props> = (props) => {
  const { visible, columns, onCancel, onSubmit ,values} = props;
  // eslint-disable-next-line react-hooks/rules-of-hooks
  const formRef = useRef<any>();
  // eslint-disable-next-line react-hooks/rules-of-hooks
  useEffect(()=>{
    if(formRef){
      formRef.current?.setFieldsValue(values);
    }

  },[values])
  return (
    <Modal visible={visible} footer={null} onCancel={() => onCancel?.()}>
      <ProTable
        type="form"
        columns={columns}
        formRef={formRef}
        onSubmit={async (value) => {
          onSubmit?.(value);
        }}
      />
    </Modal>
  );
};
export default updateForm;
