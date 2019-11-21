import React from 'react';
import { connect } from 'react-redux';
import { Container, Card, CardHeader, CardBody, Button } from 'reactstrap';
import Webcam from 'react-webcam';

class ValidatePhoto extends React.Component<any, any> {
  videoConstraints = {
    width: 360,
    height: 360,
    facingMode: 'user',
  };
  webcam: any;

  constructor(props:any) {
      super(props);
      this.webcam = React.createRef();
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    console.log(this.webcam);
    const sc = this.webcam.current.getScreenshot();
    console.log(sc);
  }

  render() {
    return (
      <section className='user-profile'>
        <Container className='mt-4 login-container'>
          <Card className='bg-light medium-card text-center shadow border-0'>
            <CardHeader className='bg-primary text-light'>
              Validate Card
            </CardHeader>
            <CardBody>
              <Webcam
                audio={false}
                screenshotFormat='image/jpeg'
                videoConstraints={this.videoConstraints}
                ref={this.webcam}
              ></Webcam>
              <Button color="primary" onClick={this.handleClick} className="mt-1">
                  Capture Photo
              </Button>
            </CardBody>
          </Card>
        </Container>
      </section>
    );
  }
}

const mapStateToProps = (state: any) => ({
  ...state,
  user: state.UserReducer,
});

export default connect(mapStateToProps)(ValidatePhoto);
