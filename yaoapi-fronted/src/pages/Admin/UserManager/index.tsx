import type {ActionType, ProColumns, ProDescriptionsItemProps} from '@ant-design/pro-components';
import {PageContainer, ProDescriptions, ProTable} from '@ant-design/pro-components';
import '@umijs/max';
import {Button, Drawer, Image, message} from 'antd';
import React, {useRef, useState} from 'react';
import type {SortOrder} from 'antd/es/table/interface';
import {
    addUserUsingPOST,
    deleteUserUsingPOST,
    listUserByPageUsingGET, updateUserByAdminUsingPOST
} from "@/services/yaoapi-backend/userController";
import UpdateModal from "@/pages/Admin/UserManager/components/UpdateModal";
import CreateModal from "@/pages/Admin/UserManager/components/CreateModal";

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
    const [currentRow, setCurrentRow] = useState<API.UserVO>();
    const [setSelectedRows] = useState<API.UserVO[]>([]);

    /**
     * @en-US Update node
     * @zh-CN 更新节点
     *
     * @param fields
     */
    const handleUpdate = async (fields: API.UserVO) => {
        if (!currentRow) {
            return;
        }
        const hide = message.loading('修改中');
        try {
            await updateUserByAdminUsingPOST({
                id: currentRow.id,
                ...fields,
            });
            hide();
            message.success('操作成功');
            actionRef.current?.reload();
            return true;
        } catch (error: any) {
            hide();
            message.error('操作失败' + error.message);
            return false;
        }
    };

    /**
     * @en-US Add node
     * @zh-CN 添加节点
     * @param fields
     */
    const handleAdd = async (fields: API.UserAddRequest) => {
        const hide = message.loading('正在添加');
        try {
            await addUserUsingPOST({
                ...fields,
            });
            hide();
            message.success('创建成功');
            handleModalVisible(false);
            actionRef.current?.reload();
            return true;
        } catch (error: any) {
            hide();
            message.error('创建失败，' + error.message);
            return false;
        }
    };

    /**
     *  Delete node
     * @zh-CN 删除节点
     *
     * @param record
     */
    const handleRemove = async (record: API.UserVO) => {
        const hide = message.loading('正在删除');
        if (!record) return true;
        try {
            await deleteUserUsingPOST({
                id: record.id,
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

    const columns: ProColumns<API.UserVO>[] = [
        {
            title: 'id',
            dataIndex: 'id',
            valueType: 'index',
            width: 50,
        },
        {
            title: '用户昵称',
            dataIndex: 'userName',
            valueType: 'text',
        },
        {
            title: '用户账号',
            dataIndex: 'userAccount',
            valueType: 'text',
            formItemProps: {
                rules: [
                    {
                        required: true,
                    },
                ],
            },
            ellipsis: true,
        },

        {
            title: '用户头像',
            dataIndex: 'userAvatar',
            valueType: 'text',
            render: (_, record) => (
                <div>
                    <Image src={record.userAvatar} width={100}/>
                </div>
            ),
            hideInSearch: true,
        },
        {
            title: '性别',
            dataIndex: 'gender',
            valueType: 'text',
            width: 50,
            valueEnum: new Map([
                [1, '女'],
                [0, '男'],
            ]),
        },
        {
            title: '角色',
            dataIndex: 'userRole',
            valueEnum: {
                user: {
                    text: '用户',
                    status: 'Default',
                },
                admin: {
                    text: '管理员',
                    status: 'Processing',
                },
            },
            formItemProps: {
                rules: [
                    {
                        required: true,
                    },
                ],
            },
        },

        {
            title: '邮箱',
            dataIndex: 'email',
            valueType: 'text',
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            valueType: 'dateTime',
            hideInForm: true,
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
            valueType: 'dateTime',
            hideInTable: true,
            hideInForm: true,
            hideInSearch: true,
        },
        {
            title: '操作',
            dataIndex: 'option',
            valueType: 'option',
            render: (_, record) => [
                <Button
                    size={'small'}
                    type={'text'}
                    key="edit"
                    onClick={() => {
                        handleUpdateModalVisible(true);
                        setCurrentRow(record);
                    }}
                >
                    修改
                </Button>,
                <Button
                    type={'text'}
                    size={'small'}
                    danger
                    key="remove"
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
                        const res: any = await listUserByPageUsingGET({
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
                // @ts-ignore
                columns={columns}
                rowSelection={{
                    onChange: (_, selectedRows) => {
                        // @ts-ignore
                        setSelectedRows(selectedRows);
                    },
                }}
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
                columns={columns}
            />

            <Drawer
                width={600}
                visible={showDetail}
                onClose={() => {
                    setCurrentRow(undefined);
                    setShowDetail(false);
                }}
                closable={false}
            >
                {currentRow?.userAccount && (
                    <ProDescriptions<API.RuleListItem>
                        column={2}
                        title={currentRow?.userAccount}
                        request={async () => ({
                            data: currentRow || {},
                        })}
                        params={{
                            id: currentRow?.userAccount,
                        }}
                        columns={columns as ProDescriptionsItemProps<API.RuleListItem>[]}
                    />
                )}
            </Drawer>
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
        </PageContainer>
    );
};
export default TableList;
