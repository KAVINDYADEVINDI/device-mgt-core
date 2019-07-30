import React from "react";
import '../../../../App.css';
import {Skeleton, Typography, Row, Col, Card, message, notification, Breadcrumb, Icon} from "antd";
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import axios from "axios";
import {withConfigContext} from "../../../../context/ConfigContext";
import {Link} from "react-router-dom";

const {Title} = Typography;

class Release extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            loading: true,
            app: null,
            uuid: null
        };
    }

    componentDidMount() {
        const {uuid, deviceType} = this.props.match.params;
        this.fetchData(uuid);
        this.props.changeSelectedMenuItem(deviceType);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.uuid !== this.state.uuid) {
            const {uuid, deviceType} = this.props.match.params;
            this.fetchData(uuid);
            this.props.changeSelectedMenuItem(deviceType);
        }
    }

    fetchData = (uuid) => {
        const config = this.props.context;

        //send request to the invoker
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/applications/" + uuid,
        ).then(res => {
            if (res.status === 200) {
                let app = res.data.data;

                this.setState({
                    app: app,
                    loading: false,
                    uuid: uuid
                })
            }

        }).catch((error) => {
            console.log(error);
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load releases.",
                });
            }

            this.setState({loading: false});
        });
    };

    render() {
        const {app, loading} = this.state;
        const {deviceType} = this.props.match.params;

        let content = <Title level={3}>No Releases Found</Title>;
        let appName = "loading...";

        if (app != null && app.applicationReleases.length !== 0) {
            content = <ReleaseView app={app} deviceType={deviceType}/>;
            appName = app.name;
        }

        return (
            <div style={{background: '#f0f2f5', minHeight: 780}}>
                <Row style={{padding: 10}}>
                    <Col lg={4}>

                    </Col>
                    <Col lg={16} md={24} style={{padding: 3}}>
                        <Breadcrumb style={{paddingBottom: 16}}>
                            <Breadcrumb.Item>
                                <Link to={"/store/"+deviceType}><Icon type="home"/> {deviceType + " apps"} </Link>
                            </Breadcrumb.Item>
                            <Breadcrumb.Item>{appName}</Breadcrumb.Item>
                        </Breadcrumb>
                        <Card>
                            <Skeleton loading={loading} avatar={{size: 'large'}} active paragraph={{rows: 8}}>
                                {content}
                            </Skeleton>
                        </Card>
                    </Col>
                </Row>

            </div>
        );
    }
}


export default withConfigContext(Release);
