import type {ActionType, ProColumns, ProDescriptionsItemProps} from '@ant-design/pro-components';
import {
    PageContainer,
    ProDescriptions,
    ProTable,
} from '@ant-design/pro-components';
import {Button, Drawer, message} from 'antd';
import React, {useRef, useState} from 'react';
import {SortOrder} from "antd/es/table/interface";
import CreateModal from "@/pages/Admin/InterfaceInfo/components/CreateModal";
import UpdateModal from "./components/UpdateModal";
import {
    addInterfaceInfoUsingPOST,
    deleteInterfaceInfoUsingPOST,
    interfaceOffLineUsingPOST,
    interfaceOnLineUsingPOST,
    listInterfaceInfoByPageUsingGET,
    updateInterfaceInfoUsingPOST
} from "@/services/yaoapi-backend/interfaceInfoController";

const TableList: React.FC = () => {
    /**
     * @en-US Pop-up window of new window
     * @zh-CN 新建窗口的弹窗
     *  */
    const [createModalVisible, handleModalVisible] = useState<boolean>(false);
    /**
     * @en-US The pop-up window of the distribution update window
     * @zh-CN 分布更新窗口的弹窗
     * */
    const [updateModalVisible, handleUpdateModalVisible] = useState<boolean>(false);
    const [showDetail, setShowDetail] = useState<boolean>(false);
    const actionRef = useRef<ActionType>();
    const [currentRow, setCurrentRow] = useState<API.InterfaceInfo>();
    const [setSelectedRows] = useState<API.InterfaceInfo[]>([]);

    /**
     * @en-US Add node
     * @zh-CN 添加节点
     * @param fields
     */
    const handleAdd = async (fields: API.InterfaceInfo) => {
        const hide = message.loading('正在添加');
        try {
            const res = await addInterfaceInfoUsingPOST({
                ...fields,
            });
            if (res.code === 200) {
                message.success('接口创建成功');
            }
            hide();
            if (res.code !== 200) {
                message.error(res.description)
            }

            handleModalVisible(false);
            return true;
        } catch (error: any) {
            hide();
            message.error('创建失败，' + error.message);
            return false;
        }
    };

    /**
     * @en-US Update node
     * @zh-CN 更新节点
     *
     * @param fields
     */
    const handleUpdate = async (fields: API.InterfaceInfo) => {
        if (!currentRow) {
            return;
        }
        const hide = message.loading('修改中');
        try {
            await updateInterfaceInfoUsingPOST({
                id: currentRow.id,
                ...fields
            });
            hide();
            message.success('操作成功');
            return true;
        } catch (error: any) {
            hide();
            message.error('操作失败，' + error.message);
            return false;
        }
    };

    /**
     * 发布接口
     *
     * @param record
     */
    const handleOnline = async (record: API.IdRequest) => {
        const hide = message.loading('发布中');
        if (!record) return true;
        try {
            const res = await interfaceOnLineUsingPOST({
                id: record.id
            });
            console.log(res.code)
            hide();
            if (res.code === 200) {
                message.success('操作成功');
                actionRef.current?.reload();
                return true;
            }else{
                message.error(res.description)
            }

        } catch (error: any) {
            hide();
            console.log("error")
            message.error('操作失败，' + error.message);
            return false;
        }
    };

    /**
     * 下线接口
     *
     * @param record
     */
    const handleOffline = async (record: API.IdRequest) => {
        const hide = message.loading('发布中');
        if (!record) return true;
        try {
            await interfaceOffLineUsingPOST({
                id: record.id
            });
            hide();
            message.success('操作成功');
            actionRef.current?.reload();
            return true;
        } catch (error: any) {
            hide();
            message.error('操作失败，' + error.message);
            return false;
        }
    };

    /**
     *  Delete node
     * @zh-CN 删除节点
     *
     * @param record
     */
    const handleRemove = async (record: API.InterfaceInfo) => {
        const hide = message.loading('正在删除');
        if (!record) return true;
        try {
            await deleteInterfaceInfoUsingPOST({
                id: record.id
            });
            hide();
            message.success('删除成功');
            actionRef.current?.reload();
            return true;
        } catch (error: any) {
            hide();
            message.error('删除失败，' + error.message);
            return false;
        }
    };
    const columns: ProColumns<API.InterfaceInfo>[] = [
        {
            title: '名称',
            dataIndex: 'name',
            tip: '接口的名称',
            formItemProps: {
                rules: [{
                    required: true,
                }]
            }
        },
        {
            title: '描述',
            dataIndex: 'description',
            valueType: 'textarea',
        },
        {
            title: '请求头',
            dataIndex: 'requestHeader',
            sorter: true,
            hideInForm: true,
            valueType: 'text'
        },
        {
            title: '请求参数(示例)',
            dataIndex: 'requestParams',
            valueType: 'text',
        },
        {
            title: '响应头',
            dataIndex: 'responseHeader',
            hideInForm: true,
            valueType: 'text'
        },
        {
            title: '请求方法',
            dataIndex: 'method',
            valueType: 'text',
        },
        {
            title: 'url',
            dataIndex: 'url',
            valueType: 'text',
        },
        {
            title: '请求状态',
            sorter: true,
            dataIndex: 'status',
            valueEnum: {
                0: {
                    text: '关闭',
                    status: 'Default'
                },
                1: {
                    text: '开启',
                    status: 'Processing'
                }
            }
        },
        {
            title: '操作',
            dataIndex: 'option',
            valueType: 'option',
            render: (_, record,) => [

                <Button
                    type="text"
                    key="update"
                    onClick={() => {
                        handleUpdateModalVisible(true);
                        setCurrentRow(record);
                    }}
                >
                    修改
                </Button>,
                record.status === 0 ? <Button
                    type="text"
                    key="online"
                    onClick={() => {
                        handleOnline(record);
                    }}
                >
                    发布
                </Button> : <Button
                    type="text"
                    key="online"
                    danger
                    onClick={() => {
                        handleOffline(record);
                    }}
                >
                    下线
                </Button>,
                <Button
                    type="text"
                    key="remove"
                    size={"middle"}
                    danger
                    onClick={() => {
                        handleRemove(record);
                    }}
                >
                    删除
                </Button>,
            ],
        },

    ];

    return (
        <PageContainer>
            <ProTable<API.RuleListItem, API.PageParams>
                headerTitle={'查询表格'}
                actionRef={actionRef}
                rowKey="key"
                search={{
                    labelWidth: 120,
                }}
                toolBarRender={() => [
                    <Button
                        type="primary"
                        key="create"
                        onClick={() => {
                            handleModalVisible(true);
                        }}
                    >
                        添加
                    </Button>,
                ]}
                request={
                    //发请求，请求数据
                    async (
                        params,
                        // eslint-disable-next-line @typescript-eslint/no-unused-vars
                        sort: Record<string, SortOrder>,
                        // eslint-disable-next-line @typescript-eslint/no-unused-vars
                        filter: Record<string, React.ReactText[] | null>,
                    ) => {
                        const res: any = await listInterfaceInfoByPageUsingGET({
                            ...params,
                        });
                        //使用数据
                        if (res?.data) {
                            return {
                                data: res?.data.records || [],
                                success: true,
                                total: res?.data.total || 0,
                            };
                        } else {
                            return {
                                data: [],
                                success: false,
                                total: 0,
                            };
                        }
                    }}
                columns={columns}
                rowSelection={{
                    onChange: (_, selectedRows) => {
                        // @ts-ignore
                        setSelectedRows(selectedRows);
                    },
                }}
            />
            <CreateModal
                onSubmit={async (values) => {
                    const success = await handleAdd(values);
                    if (success) {
                        handleModalVisible(false);
                        setCurrentRow(undefined);
                        if (actionRef.current) {
                            actionRef.current.reload();
                        }
                    }
                }}
                onCancel={() => {
                    handleModalVisible(false);
                }}
                open={createModalVisible}
            />
            <UpdateModal
                onSubmit={async (value) => {
                    const success = await handleUpdate(value);
                    if (success) {
                        handleUpdateModalVisible(false);
                        setCurrentRow(undefined);
                        if (actionRef.current) {
                            actionRef.current.reload();
                        }
                    }
                }}
                onCancel={() => {
                    handleUpdateModalVisible(false);
                    if (!showDetail) {
                        setCurrentRow(undefined);
                    }
                }}
                open={updateModalVisible}
                values={currentRow || {}}
            />

            <Drawer
                width={600}
                open={showDetail}
                onClose={() => {
                    setCurrentRow(undefined);
                    setShowDetail(false);
                }}
                closable={false}
            >
                {currentRow?.name && (
                    <ProDescriptions<API.RuleListItem>
                        column={2}
                        title={currentRow?.name}
                        request={async () => ({
                            data: currentRow || {},
                        })}
                        params={{
                            id: currentRow?.name,
                        }}
                        columns={columns as ProDescriptionsItemProps<API.RuleListItem>[]}
                    />
                )}
            </Drawer>
        </PageContainer>
    );
};

export default TableList;
