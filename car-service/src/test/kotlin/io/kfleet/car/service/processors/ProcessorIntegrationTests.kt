package io.kfleet.car.service.processors

////@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
//@ExtendWith(SpringExtension::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ContextConfiguration(initializers = [KafkaContextInitializer::class])
//class ProcessorIntegrationTests {
//
//    @Autowired
//    @Output(CarsOutBindings.CARS)
//    lateinit var carOuputChannel: MessageChannel
//
//    @Autowired
//    lateinit var carsRepository: CarsRepository
//
//
//    @Test
//    fun submitCar() {
//
//        val stats = carsRepository.getCarsStateCounts().block()
//        assertNotNull(stats)
//        expect(0) { stats.size }
//
//        val carId = 1
//        val car = CarFactory.createRandom(carId)
//        val message = MessageBuilder.createMessage(car, headers(carId))
//        val sended = carOuputChannel.send(message)
//        // this must always be true - because for this output sync is false - e.g. not configured to be sync
//        assert(true) { sended }
//
//        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
//            val respCar = carsRepository.findById("$carId").block()
//
//            expect(car.getState()) { respCar!!.getState() }
//        }
//
//        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
//            val respCars = carsRepository.findAllCars().collectList().block()
//
//            expect(1) { respCars!!.size }
//        }
//
//        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
//
//            val statsAfterPushMsg = carsRepository.getCarsStateCounts().block()!!
//
//            expect(1L) { statsAfterPushMsg.get(car.getState().toString()) }
//        }
//
//    }
//}


