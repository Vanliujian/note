延迟加载和分布查询一起使用

全局延迟加载功能只在分布查询时有用

- 在settings中设置，name="lazyLodingEnable" value="true"

association的fetchType属性：lazy/eager

- 当开启了全局加载的功能后，可以通过这个属性设置立即加载或者延迟加载